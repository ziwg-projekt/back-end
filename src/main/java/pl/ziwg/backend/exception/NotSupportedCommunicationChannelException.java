package pl.ziwg.backend.exception;

public class NotSupportedCommunicationChannelException  extends RuntimeException {
    public NotSupportedCommunicationChannelException(String cause) {
        super(cause);
    }
}
