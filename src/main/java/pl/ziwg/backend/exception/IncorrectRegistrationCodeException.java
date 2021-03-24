package pl.ziwg.backend.exception;

public class IncorrectRegistrationCodeException extends RuntimeException {
    public IncorrectRegistrationCodeException(String cause) {
        super(cause);
    }
}
