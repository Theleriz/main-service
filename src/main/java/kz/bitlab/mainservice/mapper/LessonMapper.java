package kz.bitlab.mainservice.mapper;

import kz.bitlab.mainservice.dto.LessonCreateRequest;
import kz.bitlab.mainservice.dto.LessonDto;
import kz.bitlab.mainservice.dto.LessonUpdateRequest;
import kz.bitlab.mainservice.entity.Lesson;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper
public interface LessonMapper {

    @Mapping(source = "chapter.id", target = "chapterId")
    LessonDto toDto(Lesson lesson);

    List<LessonDto> toDtoList(List<Lesson> lessons);

    // The chapter is resolved by the service from chapterId
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "chapter", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    @Mapping(target = "updatedTime", ignore = true)
    Lesson toEntity(LessonCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "chapter", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    @Mapping(target = "updatedTime", ignore = true)
    void updateEntity(LessonUpdateRequest request, @MappingTarget Lesson lesson);
}
