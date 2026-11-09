package kz.bitlab.mainservice.service;

import kz.bitlab.mainservice.TestData;
import kz.bitlab.mainservice.dto.AttachmentDto;
import kz.bitlab.mainservice.dto.FileDownload;
import kz.bitlab.mainservice.entity.Attachment;
import kz.bitlab.mainservice.entity.Lesson;
import kz.bitlab.mainservice.exception.BadRequestException;
import kz.bitlab.mainservice.exception.NotFoundException;
import kz.bitlab.mainservice.mapper.AttachmentMapper;
import kz.bitlab.mainservice.repository.AttachmentRepository;
import kz.bitlab.mainservice.repository.LessonRepository;
import kz.bitlab.mainservice.security.CourseAccessChecker;
import kz.bitlab.mainservice.service.impl.AttachmentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Optional;

import static kz.bitlab.mainservice.TestData.ATTACHMENT_ID;
import static kz.bitlab.mainservice.TestData.ATTACHMENT_NAME;
import static kz.bitlab.mainservice.TestData.ATTACHMENT_URL;
import static kz.bitlab.mainservice.TestData.LESSON_ID;
import static kz.bitlab.mainservice.TestData.MISSING_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceImplTest {

    private static final MockMultipartFile FILE =
            new MockMultipartFile("file", ATTACHMENT_NAME, "application/pdf", new byte[]{1, 2, 3});

    @Mock
    private AttachmentRepository attachmentRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private AttachmentMapper attachmentMapper;

    @Mock
    private FileService fileService;

    // The real access check: it is plain logic without dependencies
    @Spy
    private CourseAccessChecker courseAccessChecker;

    @InjectMocks
    private AttachmentServiceImpl attachmentService;

    @Test
    void upload_storesFileInMinioAndSavesAttachment() {
        Lesson lesson = TestData.lesson();
        when(lessonRepository.findById(LESSON_ID)).thenReturn(Optional.of(lesson));
        when(attachmentRepository.save(any(Attachment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(attachmentMapper.toDto(any(Attachment.class))).thenReturn(TestData.attachmentDto());

        AttachmentDto result = attachmentService.upload(LESSON_ID, FILE);

        assertThat(result.getId()).isEqualTo(ATTACHMENT_ID);
        ArgumentCaptor<String> objectName = ArgumentCaptor.forClass(String.class);
        verify(fileService).upload(objectName.capture(), eq(FILE));
        assertThat(objectName.getValue()).matches("lessons/5/[0-9a-f-]{36}\\.pdf");

        ArgumentCaptor<Attachment> saved = ArgumentCaptor.forClass(Attachment.class);
        verify(attachmentRepository).save(saved.capture());
        assertThat(saved.getValue().getName()).isEqualTo(ATTACHMENT_NAME);
        assertThat(saved.getValue().getUrl()).isEqualTo(objectName.getValue());
        assertThat(saved.getValue().getLesson()).isSameAs(lesson);
    }

    @Test
    void upload_fileNameWithPath_keepsOnlyFileName() {
        when(lessonRepository.findById(LESSON_ID)).thenReturn(Optional.of(TestData.lesson()));
        when(attachmentRepository.save(any(Attachment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        attachmentService.upload(LESSON_ID, new MockMultipartFile("file", "C:\\Users\\teacher\\notes.txt",
                "text/plain", new byte[]{1}));

        ArgumentCaptor<Attachment> saved = ArgumentCaptor.forClass(Attachment.class);
        verify(attachmentRepository).save(saved.capture());
        assertThat(saved.getValue().getName()).isEqualTo("notes.txt");
    }

    @Test
    void upload_emptyFile_throwsBadRequest() {
        MockMultipartFile empty = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);

        assertThrows(BadRequestException.class, () -> attachmentService.upload(LESSON_ID, empty));

        verify(fileService, never()).upload(anyString(), any());
    }

    @Test
    void upload_missingLesson_throwsNotFoundAndDoesNotStoreFile() {
        when(lessonRepository.findById(MISSING_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> attachmentService.upload(MISSING_ID, FILE));

        verify(fileService, never()).upload(anyString(), any());
        verify(attachmentRepository, never()).save(any());
    }

    @Test
    void upload_saveFails_deletesStoredFile() {
        when(lessonRepository.findById(LESSON_ID)).thenReturn(Optional.of(TestData.lesson()));
        when(attachmentRepository.save(any(Attachment.class))).thenThrow(new IllegalStateException("DB is down"));

        assertThrows(IllegalStateException.class, () -> attachmentService.upload(LESSON_ID, FILE));

        ArgumentCaptor<String> objectName = ArgumentCaptor.forClass(String.class);
        verify(fileService).upload(objectName.capture(), eq(FILE));
        verify(fileService).delete(objectName.getValue());
    }

    @Test
    void download_userWithRole_returnsFileFromMinio() {
        FileDownload file = new FileDownload(ATTACHMENT_NAME, "application/pdf", 3, new ByteArrayInputStream(new byte[3]));
        when(fileService.download(ATTACHMENT_URL, ATTACHMENT_NAME)).thenReturn(file);

        Attachment attachment = TestData.attachment();
        when(attachmentRepository.findWithCourseById(ATTACHMENT_ID)).thenReturn(Optional.of(attachment));
        Authentication user = authentication("ROLE_USER");

        FileDownload result = attachmentService.download(ATTACHMENT_ID, user);

        assertThat(result).isSameAs(file);
        verify(courseAccessChecker).checkReadAccess(user, attachment.getLesson().getChapter().getCourse());
    }

    @Test
    void download_userWithoutCourseAccess_throwsAccessDeniedAndDoesNotReadFile() {
        when(attachmentRepository.findWithCourseById(ATTACHMENT_ID)).thenReturn(Optional.of(TestData.attachment()));

        assertThrows(AccessDeniedException.class,
                () -> attachmentService.download(ATTACHMENT_ID, authentication()));

        verify(fileService, never()).download(anyString(), anyString());
    }

    @Test
    void download_missingAttachment_throwsNotFound() {
        when(attachmentRepository.findWithCourseById(MISSING_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> attachmentService.download(MISSING_ID, authentication("ROLE_ADMIN")));
    }

    @Test
    void getAllByLessonId_missingLesson_throwsNotFound() {
        when(lessonRepository.existsById(MISSING_ID)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> attachmentService.getAllByLessonId(MISSING_ID));
    }

    @Test
    void getAllByLessonId_returnsAttachments() {
        List<Attachment> attachments = List.of(TestData.attachment());
        when(lessonRepository.existsById(LESSON_ID)).thenReturn(true);
        when(attachmentRepository.findAllByLessonIdOrderByIdAsc(LESSON_ID)).thenReturn(attachments);
        when(attachmentMapper.toDtoList(attachments)).thenReturn(List.of(TestData.attachmentDto()));

        assertThat(attachmentService.getAllByLessonId(LESSON_ID)).hasSize(1);
    }

    private Authentication authentication(String... roles) {
        return new TestingAuthenticationToken("someone", null, roles);
    }
}
