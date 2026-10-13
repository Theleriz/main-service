package kz.bitlab.mainservice.service;

import kz.bitlab.mainservice.TestData;
import kz.bitlab.mainservice.dto.LessonCreateRequest;
import kz.bitlab.mainservice.dto.LessonDto;
import kz.bitlab.mainservice.dto.LessonUpdateRequest;
import kz.bitlab.mainservice.entity.Chapter;
import kz.bitlab.mainservice.entity.Lesson;
import kz.bitlab.mainservice.mapper.LessonMapper;
import kz.bitlab.mainservice.repository.ChapterRepository;
import kz.bitlab.mainservice.repository.LessonRepository;
import kz.bitlab.mainservice.service.impl.LessonServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static kz.bitlab.mainservice.TestData.CHAPTER_ID;
import static kz.bitlab.mainservice.TestData.LESSON_ID;
import static kz.bitlab.mainservice.TestData.MISSING_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LessonServiceImplTest {

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private ChapterRepository chapterRepository;

    @Mock
    private LessonMapper lessonMapper;

    @InjectMocks
    private LessonServiceImpl lessonService;

    @Test
    void getAll_returnsAllLessons() {
        List<Lesson> lessons = List.of(TestData.lesson());
        when(lessonRepository.findAllByOrderByIdAsc()).thenReturn(lessons);
        when(lessonMapper.toDtoList(lessons)).thenReturn(List.of(TestData.lessonDto()));

        List<LessonDto> result = lessonService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getContent()).isEqualTo(TestData.LESSON_CONTENT);
    }

    @Test
    void getAllByChapterId_existingChapter_returnsLessonsSortedByOrder() {
        List<Lesson> lessons = List.of(TestData.lesson());
        when(chapterRepository.existsById(CHAPTER_ID)).thenReturn(true);
        when(lessonRepository.findAllByChapterIdOrderByOrderAsc(CHAPTER_ID)).thenReturn(lessons);
        when(lessonMapper.toDtoList(lessons)).thenReturn(List.of(TestData.lessonDto()));

        List<LessonDto> result = lessonService.getAllByChapterId(CHAPTER_ID);

        assertThat(result).extracting(LessonDto::getChapterId).containsOnly(CHAPTER_ID);
        verify(lessonRepository).findAllByChapterIdOrderByOrderAsc(CHAPTER_ID);
    }

    @Test
    void getAllByChapterId_missingChapter_throwsNotFound() {
        when(chapterRepository.existsById(MISSING_ID)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> lessonService.getAllByChapterId(MISSING_ID));

        assertThat(ex.getMessage()).isEqualTo("Chapter with id 999 not found");
        verify(lessonRepository, never()).findAllByChapterIdOrderByOrderAsc(anyLong());
    }

    @Test
    void getById_existingId_returnsLesson() {
        Lesson lesson = TestData.lesson();
        when(lessonRepository.findById(LESSON_ID)).thenReturn(Optional.of(lesson));
        when(lessonMapper.toDto(lesson)).thenReturn(TestData.lessonDto());

        LessonDto result = lessonService.getById(LESSON_ID);

        assertThat(result.getId()).isEqualTo(LESSON_ID);
        assertThat(result.getChapterId()).isEqualTo(CHAPTER_ID);
    }

    @Test
    void getById_missingId_throwsNotFound() {
        when(lessonRepository.findById(MISSING_ID)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> lessonService.getById(MISSING_ID));

        assertThat(ex.getMessage()).isEqualTo("Lesson with id 999 not found");
    }

    @Test
    void create_existingChapter_savesLessonLinkedToChapter() {
        LessonCreateRequest request = TestData.lessonCreateRequest();
        Chapter chapter = TestData.chapter();
        Lesson lesson = Lesson.builder().name(request.getName()).order(request.getOrder()).build();
        when(chapterRepository.findById(CHAPTER_ID)).thenReturn(Optional.of(chapter));
        when(lessonMapper.toEntity(request)).thenReturn(lesson);
        when(lessonRepository.save(lesson)).thenReturn(lesson);
        when(lessonMapper.toDto(lesson)).thenReturn(TestData.lessonDto());

        LessonDto result = lessonService.create(request);

        assertThat(result.getId()).isEqualTo(LESSON_ID);
        assertThat(lesson.getChapter()).isSameAs(chapter);
        verify(lessonRepository).save(any(Lesson.class));
    }

    @Test
    void create_missingChapter_throwsNotFoundAndDoesNotSave() {
        LessonCreateRequest request = TestData.lessonCreateRequest();
        request.setChapterId(MISSING_ID);
        when(chapterRepository.findById(MISSING_ID)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> lessonService.create(request));

        assertThat(ex.getMessage()).isEqualTo("Chapter with id 999 not found");
        verify(lessonRepository, never()).save(any());
    }

    @Test
    void update_existingId_updatesAndReturnsLesson() {
        LessonUpdateRequest request = TestData.lessonUpdateRequest();
        Lesson lesson = TestData.lesson();
        LessonDto updatedDto = TestData.lessonDto();
        updatedDto.setDescription(request.getDescription());
        when(lessonRepository.findById(LESSON_ID)).thenReturn(Optional.of(lesson));
        when(lessonRepository.saveAndFlush(lesson)).thenReturn(lesson);
        when(lessonMapper.toDto(lesson)).thenReturn(updatedDto);

        LessonDto result = lessonService.update(LESSON_ID, request);

        assertThat(result.getDescription()).isEqualTo(request.getDescription());
        verify(lessonMapper).updateEntity(request, lesson);
        verify(lessonRepository).saveAndFlush(lesson);
    }

    @Test
    void update_missingId_throwsNotFoundAndDoesNotSave() {
        when(lessonRepository.findById(MISSING_ID)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> lessonService.update(MISSING_ID, TestData.lessonUpdateRequest()));

        verify(lessonRepository, never()).saveAndFlush(any());
    }

    @Test
    void delete_existingId_deletesLesson() {
        when(lessonRepository.existsById(LESSON_ID)).thenReturn(true);

        lessonService.delete(LESSON_ID);

        verify(lessonRepository).deleteById(LESSON_ID);
    }

    @Test
    void delete_missingId_throwsNotFoundAndDoesNotDelete() {
        when(lessonRepository.existsById(MISSING_ID)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> lessonService.delete(MISSING_ID));

        verify(lessonRepository, never()).deleteById(anyLong());
    }
}
