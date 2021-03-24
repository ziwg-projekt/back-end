package pl.ziwg.backend.exception;

public class NoCodeForGivenPeselException extends RuntimeException {
    public NoCodeForGivenPeselException(String cause) {
        super(cause);
    }
}
