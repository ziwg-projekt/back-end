package pl.ziwg.backend.exception;

public class UserAlreadyRegisteredException extends RuntimeException {
    public UserAlreadyRegisteredException(String cause) {
        super(cause);
    }
}
