package at.willhaben.kafka.serializers.helpers;

import at.willhaben.kafka.serializers.wrapping.MessageWrapper;
import at.willhaben.kafka.serializers.wrapping.MessageWrapperException;

public class MockMessageWrapper implements MessageWrapper {
    @Override
    public byte[] unwrapMessage(byte[] data) throws MessageWrapperException {
        return data;
    }

    @Override
    public byte[] wrapMessage(byte[] data) {
        return data;
    }
}
