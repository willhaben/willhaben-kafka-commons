package at.willhaben.kafka.serializers.helpers;

import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class MockedDelegateSerializer implements Serializer<String> {
    public static final String DELEGATE_SERIALIZER_CONFIG = "some.serializer.config";

    private String configValue;

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        configValue = (String) configs.get(DELEGATE_SERIALIZER_CONFIG);

    }

    @Override
    public byte[] serialize(String topic, String data) {
        return data.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] serialize(String topic, Headers headers, String data) {
        return data.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void close() {

    }

    public String getConfigValue() {
        return configValue;
    }
}
