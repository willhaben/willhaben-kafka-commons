package at.willhaben.kafka.serializers.wrapping;

import at.willhaben.kafka.serializers.DelegatingSerializer;
import org.apache.kafka.common.header.Headers;

import java.util.Map;

public class MessageWrappingSerializer<T> extends DelegatingSerializer<T> implements MessageWrappingSerDe {

    private MessageWrapper wrapper;

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        if (isKey) {
            throw new IllegalArgumentException("Message integrity checks should only be done on the message, not the key");
        }

        wrapper = (MessageWrapper) configs.get(MESSAGE_WRAPPER);

        if (wrapper == null) {
            throw new IllegalArgumentException("A message wrapper must be set");
        }

        super.configure(configs, true);
    }

    @Override
    protected byte[] afterSerialization(String topic, Headers headers, byte[] data) {
        return wrapper.wrapMessage(data);
    }
}
