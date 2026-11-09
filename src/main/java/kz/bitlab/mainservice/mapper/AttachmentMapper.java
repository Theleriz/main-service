package kz.bitlab.mainservice.mapper;

import kz.bitlab.mainservice.dto.AttachmentDto;
import kz.bitlab.mainservice.entity.Attachment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper
public interface AttachmentMapper {

    @Mapping(source = "lesson.id", target = "lessonId")
    AttachmentDto toDto(Attachment attachment);

    List<AttachmentDto> toDtoList(List<Attachment> attachments);
}
