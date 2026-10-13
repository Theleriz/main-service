package kz.bitlab.mainservice.service.impl;

import kz.bitlab.mainservice.dto.LessonCreateRequest;
import kz.bitlab.mainservice.dto.LessonDto;
import kz.bitlab.mainservice.dto.LessonUpdateRequest;
import kz.bitlab.mainservice.entity.Chapter;
import kz.bitlab.mainservice.entity.Lesson;
import kz.bitlab.mainservice.exception.NotFoundException;
import kz.bitlab.mainservice.mapper.LessonMapper;
import kz.bitlab.mainservice.repository.ChapterRepository;
import kz.bitlab.mainservice.repository.LessonRepository;
import kz.bitlab.mainservice.service.LessonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements LessonService {

    private final LessonRepository lessonRepository;
    private final ChapterRepository chapterRepository;
    private final LessonMapper lessonMapper;

    @Override
    @Transactional(readOnly = true)
    public List<LessonDto> getAll() {
        log.info("Fetching all lessons");
        return lessonMapper.toDtoList(lessonRepository.findAllByOrderByIdAsc());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LessonDto> getAllByChapterId(Long chapterId) {
        log.info("Fetching lessons of chapter with id {}", chapterId);
        if (!chapterRepository.existsById(chapterId)) {
            throw new NotFoundException("Chapter with id " + chapterId + " not found");
        }
        return lessonMapper.toDtoList(lessonRepository.findAllByChapterIdOrderByOrderAsc(chapterId));
    }

    @Override
    @Transactional(readOnly = true)
    public LessonDto getById(Long id) {
        log.info("Fetching lesson with id {}", id);
        return lessonMapper.toDto(findLesson(id));
    }

    @Override
    @Transactional
    public LessonDto create(LessonCreateRequest request) {
        log.info("Creating new lesson");
        log.debug("Lesson create request: {}", request);
        Chapter chapter = chapterRepository.findById(request.getChapterId())
                .orElseThrow(() -> new NotFoundException("Chapter with id " + request.getChapterId() + " not found"));
        Lesson lesson = lessonMapper.toEntity(request);
        lesson.setChapter(chapter);
        LessonDto saved = lessonMapper.toDto(lessonRepository.save(lesson));
        log.info("Lesson created with id {}", saved.getId());
        log.debug("Lesson saved: {}", saved);
        return saved;
    }

    @Override
    @Transactional
    public LessonDto update(Long id, LessonUpdateRequest request) {
        log.info("Updating lesson with id {}", id);
        log.debug("Lesson update request: {}", request);
        Lesson lesson = findLesson(id);
        lessonMapper.updateEntity(request, lesson);
        LessonDto updated = lessonMapper.toDto(lessonRepository.saveAndFlush(lesson));
        log.debug("Lesson updated: {}", updated);
        return updated;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Deleting lesson with id {}", id);
        if (!lessonRepository.existsById(id)) {
            throw new NotFoundException("Lesson with id " + id + " not found");
        }
        lessonRepository.deleteById(id);
        log.info("Lesson with id {} deleted", id);
    }

    private Lesson findLesson(Long id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Lesson with id " + id + " not found"));
    }
}
