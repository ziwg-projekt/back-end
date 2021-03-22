package pl.ziwg.backend.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(Long id, String resourceName) {
        super("Could not find " + resourceName + " with id = " + id);
    }

    public ResourceNotFoundException(String code, String resourceName) {
        super("Could not find " + resourceName + " with code = " + code);
    }
}
