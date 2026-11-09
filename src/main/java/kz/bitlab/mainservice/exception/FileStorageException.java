package kz.bitlab.mainservice.exception;

/**
 * Thrown when MinIO cannot store or return a file. Handled as 500 INTERNAL SERVER ERROR.
 */
public class FileStorageException extends RuntimeException {

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
