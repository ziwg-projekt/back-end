package pl.ziwg.backend.exception;

public class PeselNotCorrespondingToTokenException extends RuntimeException {
    public PeselNotCorrespondingToTokenException(String cause) {
        super(cause);
    }
}
