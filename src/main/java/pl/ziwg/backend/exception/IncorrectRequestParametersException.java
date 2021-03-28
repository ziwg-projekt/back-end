package pl.ziwg.backend.exception;

public class IncorrectRequestParametersException extends RuntimeException {
    public IncorrectRequestParametersException(String cause) {
        super(cause);
    }
}
