package at.willhaben.kafka.serializers.helpers;

import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public class MockedDelegateDeserializer implements Deserializer<String> {
    public static final String DELEGATE_DESERIALIZER_CONFIG = "some.deserializer.config";

    private String configValue;

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        configValue = (String) configs.get(DELEGATE_DESERIALIZER_CONFIG);
    }

    @Override
    public String deserialize(String topic, byte[] data) {
        return new String(data);
    }

    @Override
    public String deserialize(String topic, Headers headers, byte[] data) {
        return new String(data);
    }

    @Override
    public void close() {
    }

    public String getConfigValue() {
        return configValue;
    }
}
