package at.willhaben.kafka.serializers;

import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

/**
 * Takes a delegate that does the actual serialization and provides the possibility to run code before and/or after.
 */
public abstract class DelegatingSerializer<T> extends DelegatingSerDe implements Serializer<T> {
    public static final String SERIALIZER_CLASS = "delegate.serializer";
    private Serializer<T> delegate;

    public DelegatingSerializer() {
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        Class<Serializer<T>> delegateClass = (Class<Serializer<T>>) configs.get(SERIALIZER_CLASS);

        this.delegate = createDelegate(delegateClass, Serializer.class);
        delegate.configure(configs, isKey);
    }

    @Override
    public byte[] serialize(String topic, T Object) {
        Headers headers = new RecordHeaders();
        T changedObject = beforeSerialization(topic, headers, Object);
        byte[] data = delegate.serialize(topic, headers, changedObject);
        return afterSerialization(topic, headers, data);
    }

    @Override
    public byte[] serialize(String topic, Headers headers, T object) {
        T changedObject = beforeSerialization(topic, headers, object);
        byte[] data = delegate.serialize(topic, headers, changedObject);
        return afterSerialization(topic, headers, data);
    }

    @Override
    public void close() {

    }

    public Serializer<T> getDelegate() {
        return delegate;
    }

    protected T beforeSerialization(String topic, Headers headers, T object) {
        return object;
    }

    protected byte[] afterSerialization(String topic, Headers headers, byte[] data) {
        return data;
    }
}
