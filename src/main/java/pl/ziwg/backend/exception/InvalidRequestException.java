package pl.ziwg.backend.exception;

public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String cause) {
        super(cause);
    }
}
