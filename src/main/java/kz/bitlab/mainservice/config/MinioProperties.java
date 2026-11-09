package kz.bitlab.mainservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Connection to MinIO (S3). The bucket is created by the minio-init container in docker-compose.yml.
 */
@ConfigurationProperties(prefix = "minio")
public record MinioProperties(String url, String accessKey, String secretKey, String bucket) {
}
