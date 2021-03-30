package pl.ziwg.backend.exception;

public class MyFileNotFoundException extends RuntimeException {
    public MyFileNotFoundException(String cause) {
        super(cause);
    }
}

