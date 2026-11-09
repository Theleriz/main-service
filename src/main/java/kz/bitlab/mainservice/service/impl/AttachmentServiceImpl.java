package kz.bitlab.mainservice.service.impl;

import kz.bitlab.mainservice.dto.AttachmentDto;
import kz.bitlab.mainservice.dto.FileDownload;
import kz.bitlab.mainservice.entity.Attachment;
import kz.bitlab.mainservice.entity.Lesson;
import kz.bitlab.mainservice.exception.BadRequestException;
import kz.bitlab.mainservice.exception.NotFoundException;
import kz.bitlab.mainservice.mapper.AttachmentMapper;
import kz.bitlab.mainservice.repository.AttachmentRepository;
import kz.bitlab.mainservice.repository.LessonRepository;
import kz.bitlab.mainservice.security.CourseAccessChecker;
import kz.bitlab.mainservice.service.AttachmentService;
import kz.bitlab.mainservice.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {

    private static final int MAX_NAME_LENGTH = 255;
    private static final String DEFAULT_FILE_NAME = "file";

    private final AttachmentRepository attachmentRepository;
    private final LessonRepository lessonRepository;
    private final AttachmentMapper attachmentMapper;
    private final FileService fileService;
    private final CourseAccessChecker courseAccessChecker;

    @Override
    @Transactional
    public AttachmentDto upload(Long lessonId, MultipartFile file) {
        log.info("Uploading attachment for lesson with id {}", lessonId);
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File must not be empty");
        }
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new NotFoundException("Lesson with id " + lessonId + " not found"));

        String name = fileName(file);
        String objectName = objectName(lessonId, name);
        fileService.upload(objectName, file);

        Attachment attachment = Attachment.builder()
                .name(name)
                .url(objectName)
                .lesson(lesson)
                .build();
        try {
            attachment = attachmentRepository.save(attachment);
        } catch (RuntimeException e) {
            // Do not leave a file in MinIO that no attachment points to
            fileService.delete(objectName);
            throw e;
        }
        log.info("Attachment {} saved with id {} as {}", name, attachment.getId(), objectName);
        return attachmentMapper.toDto(attachment);
    }

    @Override
    @Transactional(readOnly = true)
    public FileDownload download(Long id, Authentication authentication) {
        log.info("Downloading attachment with id {}", id);
        Attachment attachment = attachmentRepository.findWithCourseById(id)
                .orElseThrow(() -> new NotFoundException("Attachment with id " + id + " not found"));
        courseAccessChecker.checkReadAccess(authentication, attachment.getLesson().getChapter().getCourse());
        return fileService.download(attachment.getUrl(), attachment.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttachmentDto> getAllByLessonId(Long lessonId) {
        log.info("Fetching attachments of lesson with id {}", lessonId);
        if (!lessonRepository.existsById(lessonId)) {
            throw new NotFoundException("Lesson with id " + lessonId + " not found");
        }
        return attachmentMapper.toDtoList(attachmentRepository.findAllByLessonIdOrderByIdAsc(lessonId));
    }

    private String fileName(MultipartFile file) {
        // Browsers may send a full path, keep only the file name
        String name = StringUtils.getFilename(StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : ""));
        if (!StringUtils.hasText(name)) {
            return DEFAULT_FILE_NAME;
        }
        return name.length() > MAX_NAME_LENGTH ? name.substring(name.length() - MAX_NAME_LENGTH) : name;
    }

    /**
     * Unique object name that fits into VARCHAR(255): lessons/{lessonId}/{uuid}.{ext}
     */
    private String objectName(Long lessonId, String fileName) {
        String extension = StringUtils.getFilenameExtension(fileName);
        String suffix = extension != null && extension.matches("[A-Za-z0-9]{1,10}")
                ? "." + extension.toLowerCase(Locale.ROOT)
                : "";
        return "lessons/" + lessonId + "/" + UUID.randomUUID() + suffix;
    }
}
