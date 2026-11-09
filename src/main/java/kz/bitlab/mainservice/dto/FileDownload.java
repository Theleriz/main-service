package kz.bitlab.mainservice.dto;

import java.io.InputStream;

/**
 * A file read from the storage, ready to be streamed to the client.
 *
 * @param name        file name shown to the user (Content-Disposition)
 * @param contentType MIME type stored with the file
 * @param size        size in bytes
 * @param content     file content, closed after the response is written
 */
public record FileDownload(String name, String contentType, long size, InputStream content) {
}
