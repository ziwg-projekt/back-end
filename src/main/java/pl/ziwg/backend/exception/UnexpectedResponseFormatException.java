package pl.ziwg.backend.exception;

public class UnexpectedResponseFormatException extends RuntimeException {
    public UnexpectedResponseFormatException(String cause) {
        super(cause);
    }
}
