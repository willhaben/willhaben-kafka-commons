package at.willhaben.kafka.serializers.wrapping;

public class MessageWrapperException extends RuntimeException {
    public MessageWrapperException(String message) {
        super(message);
    }

    public MessageWrapperException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageWrapperException(Throwable cause) {
        super(cause);
    }
}
