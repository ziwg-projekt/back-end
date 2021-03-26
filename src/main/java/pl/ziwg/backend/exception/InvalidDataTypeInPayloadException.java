package pl.ziwg.backend.exception;

public class InvalidDataTypeInPayloadException extends RuntimeException {
    public InvalidDataTypeInPayloadException(String cause) {
        super(cause);
    }
}
