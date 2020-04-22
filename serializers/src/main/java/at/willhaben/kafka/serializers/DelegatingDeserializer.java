package at.willhaben.kafka.serializers;

import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

/**
 * Takes a delegate that does the actual deserialization and provides the possibility to run code before and/or after.
 */
public abstract class DelegatingDeserializer<T> extends DelegatingSerDe implements Deserializer<T> {
    public static final String DESERIALIZER_CLASS = "delegate.deserializer";
    private Deserializer<T> delegate;

    public DelegatingDeserializer() {
    }

    @Override
    @SuppressWarnings("unchecked")
    public void configure(Map<String, ?> configs, boolean isKey) {
        Class<Deserializer<T>> delegateClass = (Class<Deserializer<T>>) configs.get(DESERIALIZER_CLASS);

        this.delegate = createDelegate(delegateClass, Deserializer.class);
        delegate.configure(configs, isKey);
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        Headers headers = new RecordHeaders();
        byte[] changedData = beforeDeserialization(topic, headers, data);
        T object = delegate.deserialize(topic, headers, changedData);
        return afterDeserialization(topic, headers, object);
    }

    @Override
    public T deserialize(String topic, Headers headers, byte[] data) {
        byte[] changedData = beforeDeserialization(topic, headers, data);
        T object = delegate.deserialize(topic, headers, changedData);
        return afterDeserialization(topic, headers, object);
    }

    @Override
    public void close() {

    }

    public Deserializer<T> getDelegate() {
        return delegate;
    }

    protected byte[] beforeDeserialization(String topic, Headers headers, byte[] data) {
        return data;
    }

    protected T afterDeserialization(String topic, Headers headers, T object) {
        return object;
    }
}
