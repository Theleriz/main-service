package kz.bitlab.mainservice.service.impl;

import kz.bitlab.mainservice.dto.CourseCreateRequest;
import kz.bitlab.mainservice.dto.CourseDto;
import kz.bitlab.mainservice.dto.CourseUpdateRequest;
import kz.bitlab.mainservice.entity.Course;
import kz.bitlab.mainservice.exception.NotFoundException;
import kz.bitlab.mainservice.mapper.CourseMapper;
import kz.bitlab.mainservice.repository.CourseRepository;
import kz.bitlab.mainservice.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CourseDto> getAll() {
        log.info("Fetching all courses");
        return courseMapper.toDtoList(courseRepository.findAllByOrderByIdAsc());
    }

    @Override
    @Transactional(readOnly = true)
    public CourseDto getById(Long id) {
        log.info("Fetching course with id {}", id);
        return courseMapper.toDto(findCourse(id));
    }

    @Override
    @Transactional
    public CourseDto create(CourseCreateRequest request) {
        log.info("Creating new course");
        log.debug("Course create request: {}", request);
        Course course = courseMapper.toEntity(request);
        CourseDto saved = courseMapper.toDto(courseRepository.save(course));
        log.info("Course created with id {}", saved.getId());
        log.debug("Course saved: {}", saved);
        return saved;
    }

    @Override
    @Transactional
    public CourseDto update(Long id, CourseUpdateRequest request) {
        log.info("Updating course with id {}", id);
        log.debug("Course update request: {}", request);
        Course course = findCourse(id);
        courseMapper.updateEntity(request, course);
        CourseDto updated = courseMapper.toDto(courseRepository.saveAndFlush(course));
        log.debug("Course updated: {}", updated);
        return updated;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Deleting course with id {}", id);
        if (!courseRepository.existsById(id)) {
            throw new NotFoundException("Course with id " + id + " not found");
        }
        // Chapters and lessons are removed by ON DELETE CASCADE
        courseRepository.deleteById(id);
        log.info("Course with id {} deleted", id);
    }

    private Course findCourse(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Course with id " + id + " not found"));
    }
}
