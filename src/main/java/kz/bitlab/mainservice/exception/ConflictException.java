package kz.bitlab.mainservice.exception;

/**
 * Thrown when a user with the same username or email already exists. Handled as 409 CONFLICT.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
