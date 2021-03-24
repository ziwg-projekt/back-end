package pl.ziwg.backend.exception;

public class PeselDoesNotExistsException extends RuntimeException {
    public PeselDoesNotExistsException(String cause) {
        super(cause);
    }
}
