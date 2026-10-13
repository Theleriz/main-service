package kz.bitlab.mainservice.service;

import kz.bitlab.mainservice.TestData;
import kz.bitlab.mainservice.dto.ChapterCreateRequest;
import kz.bitlab.mainservice.dto.ChapterDto;
import kz.bitlab.mainservice.dto.ChapterUpdateRequest;
import kz.bitlab.mainservice.entity.Chapter;
import kz.bitlab.mainservice.entity.Course;
import kz.bitlab.mainservice.mapper.ChapterMapper;
import kz.bitlab.mainservice.repository.ChapterRepository;
import kz.bitlab.mainservice.repository.CourseRepository;
import kz.bitlab.mainservice.service.impl.ChapterServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static kz.bitlab.mainservice.TestData.CHAPTER_ID;
import static kz.bitlab.mainservice.TestData.COURSE_ID;
import static kz.bitlab.mainservice.TestData.MISSING_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChapterServiceImplTest {

    @Mock
    private ChapterRepository chapterRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private ChapterMapper chapterMapper;

    @InjectMocks
    private ChapterServiceImpl chapterService;

    @Test
    void getAll_returnsAllChapters() {
        List<Chapter> chapters = List.of(TestData.chapter());
        when(chapterRepository.findAllByOrderByIdAsc()).thenReturn(chapters);
        when(chapterMapper.toDtoList(chapters)).thenReturn(List.of(TestData.chapterDto()));

        List<ChapterDto> result = chapterService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo(TestData.CHAPTER_NAME);
    }

    @Test
    void getAllByCourseId_existingCourse_returnsChaptersSortedByOrder() {
        List<Chapter> chapters = List.of(TestData.chapter());
        when(courseRepository.existsById(COURSE_ID)).thenReturn(true);
        when(chapterRepository.findAllByCourseIdOrderByOrderAsc(COURSE_ID)).thenReturn(chapters);
        when(chapterMapper.toDtoList(chapters)).thenReturn(List.of(TestData.chapterDto()));

        List<ChapterDto> result = chapterService.getAllByCourseId(COURSE_ID);

        assertThat(result).extracting(ChapterDto::getCourseId).containsOnly(COURSE_ID);
        verify(chapterRepository).findAllByCourseIdOrderByOrderAsc(COURSE_ID);
    }

    @Test
    void getAllByCourseId_missingCourse_throwsNotFound() {
        when(courseRepository.existsById(MISSING_ID)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> chapterService.getAllByCourseId(MISSING_ID));

        assertThat(ex.getMessage()).isEqualTo("Course with id 999 not found");
        verify(chapterRepository, never()).findAllByCourseIdOrderByOrderAsc(anyLong());
    }

    @Test
    void getById_existingId_returnsChapter() {
        Chapter chapter = TestData.chapter();
        when(chapterRepository.findById(CHAPTER_ID)).thenReturn(Optional.of(chapter));
        when(chapterMapper.toDto(chapter)).thenReturn(TestData.chapterDto());

        ChapterDto result = chapterService.getById(CHAPTER_ID);

        assertThat(result.getId()).isEqualTo(CHAPTER_ID);
        assertThat(result.getCourseId()).isEqualTo(COURSE_ID);
    }

    @Test
    void getById_missingId_throwsNotFound() {
        when(chapterRepository.findById(MISSING_ID)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> chapterService.getById(MISSING_ID));

        assertThat(ex.getMessage()).isEqualTo("Chapter with id 999 not found");
    }

    @Test
    void create_existingCourse_savesChapterLinkedToCourse() {
        ChapterCreateRequest request = TestData.chapterCreateRequest();
        Course course = TestData.course();
        Chapter chapter = Chapter.builder().name(request.getName()).order(request.getOrder()).build();
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));
        when(chapterMapper.toEntity(request)).thenReturn(chapter);
        when(chapterRepository.save(chapter)).thenReturn(chapter);
        when(chapterMapper.toDto(chapter)).thenReturn(TestData.chapterDto());

        ChapterDto result = chapterService.create(request);

        assertThat(result.getId()).isEqualTo(CHAPTER_ID);
        assertThat(chapter.getCourse()).isSameAs(course);
        verify(chapterRepository).save(any(Chapter.class));
    }

    @Test
    void create_missingCourse_throwsNotFoundAndDoesNotSave() {
        ChapterCreateRequest request = TestData.chapterCreateRequest();
        request.setCourseId(MISSING_ID);
        when(courseRepository.findById(MISSING_ID)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> chapterService.create(request));

        assertThat(ex.getMessage()).isEqualTo("Course with id 999 not found");
        verify(chapterRepository, never()).save(any());
    }

    @Test
    void update_existingId_updatesAndReturnsChapter() {
        ChapterUpdateRequest request = TestData.chapterUpdateRequest();
        Chapter chapter = TestData.chapter();
        ChapterDto updatedDto = TestData.chapterDto();
        updatedDto.setName(request.getName());
        when(chapterRepository.findById(CHAPTER_ID)).thenReturn(Optional.of(chapter));
        when(chapterRepository.saveAndFlush(chapter)).thenReturn(chapter);
        when(chapterMapper.toDto(chapter)).thenReturn(updatedDto);

        ChapterDto result = chapterService.update(CHAPTER_ID, request);

        assertThat(result.getName()).isEqualTo(request.getName());
        verify(chapterMapper).updateEntity(request, chapter);
        verify(chapterRepository).saveAndFlush(chapter);
    }

    @Test
    void update_missingId_throwsNotFoundAndDoesNotSave() {
        when(chapterRepository.findById(MISSING_ID)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> chapterService.update(MISSING_ID, TestData.chapterUpdateRequest()));

        verify(chapterRepository, never()).saveAndFlush(any());
    }

    @Test
    void delete_existingId_deletesChapter() {
        when(chapterRepository.existsById(CHAPTER_ID)).thenReturn(true);

        chapterService.delete(CHAPTER_ID);

        verify(chapterRepository).deleteById(CHAPTER_ID);
    }

    @Test
    void delete_missingId_throwsNotFoundAndDoesNotDelete() {
        when(chapterRepository.existsById(MISSING_ID)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> chapterService.delete(MISSING_ID));

        verify(chapterRepository, never()).deleteById(anyLong());
    }
}
