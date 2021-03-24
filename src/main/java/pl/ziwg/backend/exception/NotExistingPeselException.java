package pl.ziwg.backend.exception;

public class NotExistingPeselException extends RuntimeException {
    public NotExistingPeselException(String cause) {
        super(cause);
    }
}
