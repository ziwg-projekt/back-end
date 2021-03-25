package pl.ziwg.backend.exception;

public class TokenDoesNotExistsException extends RuntimeException {
    public TokenDoesNotExistsException(String cause) {
        super(cause);
    }
}
