package pl.ziwg.backend.exception;

public class VaccineAlreadyExistsException extends RuntimeException {
    public VaccineAlreadyExistsException(String cause) {
        super(cause);
    }
}
