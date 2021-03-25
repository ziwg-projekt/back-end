package pl.ziwg.backend.exception;

public class VerificationAlreadySucceededException extends RuntimeException {
    public VerificationAlreadySucceededException(String cause) {
        super(cause);
    }
}
