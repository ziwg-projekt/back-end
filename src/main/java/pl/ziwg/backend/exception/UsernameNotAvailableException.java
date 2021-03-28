package pl.ziwg.backend.exception;

public class UsernameNotAvailableException extends RuntimeException {
    public UsernameNotAvailableException(String cause) {
        super(cause);
    }
}
