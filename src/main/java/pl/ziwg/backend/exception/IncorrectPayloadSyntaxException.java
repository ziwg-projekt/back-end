package pl.ziwg.backend.exception;

public class IncorrectPayloadSyntaxException extends RuntimeException {
    public IncorrectPayloadSyntaxException(String cause) {
        super(cause);
    }
}
