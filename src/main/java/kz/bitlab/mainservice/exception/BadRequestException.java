package kz.bitlab.mainservice.exception;

/**
 * Thrown when the request is invalid in a way bean validation cannot express. Handled as 400 BAD REQUEST.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
