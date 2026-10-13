package kz.bitlab.mainservice.service;

import kz.bitlab.mainservice.dto.ChapterCreateRequest;
import kz.bitlab.mainservice.dto.ChapterDto;
import kz.bitlab.mainservice.dto.ChapterUpdateRequest;

import java.util.List;

public interface ChapterService {

    List<ChapterDto> getAll();

    List<ChapterDto> getAllByCourseId(Long courseId);

    ChapterDto getById(Long id);

    ChapterDto create(ChapterCreateRequest request);

    ChapterDto update(Long id, ChapterUpdateRequest request);

    void delete(Long id);
}
