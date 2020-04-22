package at.willhaben.kafka.serializers.wrapping;

public interface MessageWrapper {

    byte[] unwrapMessage(byte[] data) throws MessageWrapperException;

    byte[] wrapMessage(byte[] data);
}
