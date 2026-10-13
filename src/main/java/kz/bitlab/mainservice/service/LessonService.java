package kz.bitlab.mainservice.service;

import kz.bitlab.mainservice.dto.LessonCreateRequest;
import kz.bitlab.mainservice.dto.LessonDto;
import kz.bitlab.mainservice.dto.LessonUpdateRequest;

import java.util.List;

public interface LessonService {

    List<LessonDto> getAll();

    List<LessonDto> getAllByChapterId(Long chapterId);

    LessonDto getById(Long id);

    LessonDto create(LessonCreateRequest request);

    LessonDto update(Long id, LessonUpdateRequest request);

    void delete(Long id);
}
