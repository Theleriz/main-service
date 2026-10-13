package kz.bitlab.mainservice.exception;

/**
 * Thrown when a requested object does not exist. Handled as 404 NOT FOUND.
 * Extends {@link IllegalArgumentException} as required by the specification.
 */
public class NotFoundException extends IllegalArgumentException {

    public NotFoundException(String message) {
        super(message);
    }
}
