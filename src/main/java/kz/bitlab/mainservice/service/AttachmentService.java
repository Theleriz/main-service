package kz.bitlab.mainservice.service;

import kz.bitlab.mainservice.dto.AttachmentDto;
import kz.bitlab.mainservice.dto.FileDownload;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AttachmentService {

    AttachmentDto upload(Long lessonId, MultipartFile file);

    /**
     * Checks the user's access to the course of the attachment's lesson and opens the file.
     */
    FileDownload download(Long id, Authentication authentication);

    List<AttachmentDto> getAllByLessonId(Long lessonId);
}
