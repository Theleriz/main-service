package kz.bitlab.mainservice.service;

import kz.bitlab.mainservice.dto.FileDownload;
import org.springframework.web.multipart.MultipartFile;

/**
 * File storage (MinIO / S3). Files are addressed by their object name inside the bucket.
 */
public interface FileService {

    void upload(String objectName, MultipartFile file);

    /**
     * Opens the stored file. The caller must close the returned stream.
     */
    FileDownload download(String objectName, String fileName);

    void delete(String objectName);
}
