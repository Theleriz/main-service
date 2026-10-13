package kz.bitlab.mainservice.mapper;

import kz.bitlab.mainservice.dto.ChapterCreateRequest;
import kz.bitlab.mainservice.dto.ChapterDto;
import kz.bitlab.mainservice.dto.ChapterUpdateRequest;
import kz.bitlab.mainservice.entity.Chapter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper
public interface ChapterMapper {

    @Mapping(source = "course.id", target = "courseId")
    ChapterDto toDto(Chapter chapter);

    List<ChapterDto> toDtoList(List<Chapter> chapters);

    // The course is resolved by the service from courseId
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    @Mapping(target = "updatedTime", ignore = true)
    Chapter toEntity(ChapterCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    @Mapping(target = "updatedTime", ignore = true)
    void updateEntity(ChapterUpdateRequest request, @MappingTarget Chapter chapter);
}
