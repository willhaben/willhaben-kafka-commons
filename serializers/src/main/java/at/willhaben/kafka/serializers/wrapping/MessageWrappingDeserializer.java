package at.willhaben.kafka.serializers.wrapping;

import at.willhaben.kafka.serializers.DelegatingDeserializer;
import org.apache.kafka.common.header.Headers;

import java.util.Map;

public class MessageWrappingDeserializer<T> extends DelegatingDeserializer<T> implements MessageWrappingSerDe {

    private MessageWrapper wrapper;

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        if (isKey) {
            throw new IllegalArgumentException("Message integrity checks should only be done on the message, not the key.");
        }

        wrapper = (MessageWrapper) configs.get(MESSAGE_WRAPPER);

        if (wrapper == null) {
            throw new IllegalArgumentException("A message wrapper must be set");
        }

        super.configure(configs, false);
    }

    @Override
    protected byte[] beforeDeserialization(String topic, Headers headers, byte[] data) {
        try {
            return wrapper.unwrapMessage(data);
        } catch (MessageWrapperException e) {
            throw new IllegalArgumentException("The received message could not be validated", e);
        }
    }
}
