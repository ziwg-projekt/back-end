package pl.ziwg.backend.exception;

public class RegistrationCodeExpiredException  extends RuntimeException {
    public RegistrationCodeExpiredException(String cause) {
        super(cause);
    }
}
