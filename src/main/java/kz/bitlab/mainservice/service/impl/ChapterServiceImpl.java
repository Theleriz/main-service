package kz.bitlab.mainservice.service.impl;

import kz.bitlab.mainservice.dto.ChapterCreateRequest;
import kz.bitlab.mainservice.dto.ChapterDto;
import kz.bitlab.mainservice.dto.ChapterUpdateRequest;
import kz.bitlab.mainservice.entity.Chapter;
import kz.bitlab.mainservice.entity.Course;
import kz.bitlab.mainservice.exception.NotFoundException;
import kz.bitlab.mainservice.mapper.ChapterMapper;
import kz.bitlab.mainservice.repository.ChapterRepository;
import kz.bitlab.mainservice.repository.CourseRepository;
import kz.bitlab.mainservice.service.ChapterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChapterServiceImpl implements ChapterService {

    private final ChapterRepository chapterRepository;
    private final CourseRepository courseRepository;
    private final ChapterMapper chapterMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ChapterDto> getAll() {
        log.info("Fetching all chapters");
        return chapterMapper.toDtoList(chapterRepository.findAllByOrderByIdAsc());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChapterDto> getAllByCourseId(Long courseId) {
        log.info("Fetching chapters of course with id {}", courseId);
        if (!courseRepository.existsById(courseId)) {
            throw new NotFoundException("Course with id " + courseId + " not found");
        }
        return chapterMapper.toDtoList(chapterRepository.findAllByCourseIdOrderByOrderAsc(courseId));
    }

    @Override
    @Transactional(readOnly = true)
    public ChapterDto getById(Long id) {
        log.info("Fetching chapter with id {}", id);
        return chapterMapper.toDto(findChapter(id));
    }

    @Override
    @Transactional
    public ChapterDto create(ChapterCreateRequest request) {
        log.info("Creating new chapter");
        log.debug("Chapter create request: {}", request);
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new NotFoundException("Course with id " + request.getCourseId() + " not found"));
        Chapter chapter = chapterMapper.toEntity(request);
        chapter.setCourse(course);
        ChapterDto saved = chapterMapper.toDto(chapterRepository.save(chapter));
        log.info("Chapter created with id {}", saved.getId());
        log.debug("Chapter saved: {}", saved);
        return saved;
    }

    @Override
    @Transactional
    public ChapterDto update(Long id, ChapterUpdateRequest request) {
        log.info("Updating chapter with id {}", id);
        log.debug("Chapter update request: {}", request);
        Chapter chapter = findChapter(id);
        chapterMapper.updateEntity(request, chapter);
        ChapterDto updated = chapterMapper.toDto(chapterRepository.saveAndFlush(chapter));
        log.debug("Chapter updated: {}", updated);
        return updated;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Deleting chapter with id {}", id);
        if (!chapterRepository.existsById(id)) {
            throw new NotFoundException("Chapter with id " + id + " not found");
        }
        // Lessons are removed by ON DELETE CASCADE
        chapterRepository.deleteById(id);
        log.info("Chapter with id {} deleted", id);
    }

    private Chapter findChapter(Long id) {
        return chapterRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Chapter with id " + id + " not found"));
    }
}
