package at.willhaben.kafka.serializers;

import at.willhaben.kafka.serializers.helpers.MockMessageWrapper;
import at.willhaben.kafka.serializers.helpers.MockedDelegateDeserializer;
import at.willhaben.kafka.serializers.helpers.MockedDelegateSerializer;
import at.willhaben.kafka.serializers.wrapping.MessageWrappingDeserializer;
import at.willhaben.kafka.serializers.wrapping.MessageWrappingSerDe;
import at.willhaben.kafka.serializers.wrapping.MessageWrappingSerializer;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DelegatingSerDeTest {

    @Test
    public void createWrappingSerializer_shouldCreateAndConfigureDelegate() {
        String configValue = "anyConfigValue";
        MessageWrappingSerializer<String> serializer = new MessageWrappingSerializer<>();

        Map<String, ?> config = Map.of(MockedDelegateSerializer.DELEGATE_SERIALIZER_CONFIG, configValue,
                DelegatingSerializer.SERIALIZER_CLASS, MockedDelegateSerializer.class,
                MessageWrappingSerDe.MESSAGE_WRAPPER, new MockMessageWrapper());

        serializer.configure(config, false);

        assertTrue(serializer.getDelegate() instanceof MockedDelegateSerializer);
        MockedDelegateSerializer delegate = (MockedDelegateSerializer) serializer.getDelegate();
        assertEquals(configValue, delegate.getConfigValue());
    }

    @Test
    public void createWrappingDeserializer_shouldCreateAndConfigureDelegate() {
        String configValue = "anyConfigValue";
        MessageWrappingDeserializer<String> serializer = new MessageWrappingDeserializer<>();

        Map<String, ?> config = Map.of(MockedDelegateDeserializer.DELEGATE_DESERIALIZER_CONFIG, configValue,
                DelegatingDeserializer.DESERIALIZER_CLASS, MockedDelegateDeserializer.class,
                MessageWrappingSerDe.MESSAGE_WRAPPER, new MockMessageWrapper());

        serializer.configure(config, false);

        assertTrue(serializer.getDelegate() instanceof MockedDelegateDeserializer);
        MockedDelegateDeserializer delegate = (MockedDelegateDeserializer) serializer.getDelegate();
        assertEquals(configValue, delegate.getConfigValue());
    }
}
