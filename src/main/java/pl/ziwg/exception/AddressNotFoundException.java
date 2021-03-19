package pl.ziwg.exception;

public class AddressNotFoundException extends RuntimeException {

    public AddressNotFoundException(Long id) {
        super("Could not find address " + id);
    }
}
