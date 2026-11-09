package kz.bitlab.mainservice.service.impl;

import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.ErrorResponseException;
import kz.bitlab.mainservice.config.MinioProperties;
import kz.bitlab.mainservice.dto.FileDownload;
import kz.bitlab.mainservice.exception.FileStorageException;
import kz.bitlab.mainservice.exception.NotFoundException;
import kz.bitlab.mainservice.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioFileService implements FileService {

    private static final String NO_SUCH_KEY = "NoSuchKey";

    private final MinioClient minioClient;
    private final MinioProperties properties;

    @Override
    public void upload(String objectName, MultipartFile file) {
        log.info("Uploading file {} ({} bytes) to MinIO", objectName, file.getSize());
        String contentType = file.getContentType() != null
                ? file.getContentType()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        try (InputStream content = file.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(properties.bucket())
                    .object(objectName)
                    .stream(content, file.getSize(), -1)
                    .contentType(contentType)
                    .build());
        } catch (Exception e) {
            throw new FileStorageException("Could not upload file " + objectName, e);
        }
    }

    @Override
    public FileDownload download(String objectName, String fileName) {
        log.info("Downloading file {} from MinIO", objectName);
        try {
            GetObjectResponse response = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(properties.bucket())
                    .object(objectName)
                    .build());
            String contentType = response.headers().get(HttpHeaders.CONTENT_TYPE);
            String contentLength = response.headers().get(HttpHeaders.CONTENT_LENGTH);
            return new FileDownload(
                    fileName,
                    contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE,
                    contentLength != null ? Long.parseLong(contentLength) : -1,
                    response);
        } catch (ErrorResponseException e) {
            if (NO_SUCH_KEY.equals(e.errorResponse().code())) {
                throw new NotFoundException("File " + fileName + " not found in storage");
            }
            throw new FileStorageException("Could not download file " + objectName, e);
        } catch (Exception e) {
            throw new FileStorageException("Could not download file " + objectName, e);
        }
    }

    @Override
    public void delete(String objectName) {
        log.info("Deleting file {} from MinIO", objectName);
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(properties.bucket())
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            throw new FileStorageException("Could not delete file " + objectName, e);
        }
    }
}
