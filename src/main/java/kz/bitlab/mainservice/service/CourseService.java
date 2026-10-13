package kz.bitlab.mainservice.service;

import kz.bitlab.mainservice.dto.CourseCreateRequest;
import kz.bitlab.mainservice.dto.CourseDto;
import kz.bitlab.mainservice.dto.CourseUpdateRequest;

import java.util.List;

public interface CourseService {

    List<CourseDto> getAll();

    CourseDto getById(Long id);

    CourseDto create(CourseCreateRequest request);

    CourseDto update(Long id, CourseUpdateRequest request);

    void delete(Long id);
}
