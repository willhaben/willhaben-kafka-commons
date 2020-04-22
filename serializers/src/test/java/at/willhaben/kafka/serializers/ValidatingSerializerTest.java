package at.willhaben.kafka.serializers;

import at.willhaben.kafka.serializers.helpers.MockedDelegateSerializer;
import at.willhaben.kafka.serializers.wrapping.HmacValidationWrapper;
import at.willhaben.kafka.serializers.wrapping.MessageWrappingDeserializer;
import at.willhaben.kafka.serializers.wrapping.MessageWrappingSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ValidatingSerializerTest {

    private MessageWrappingSerializer<String> serializer;

    @BeforeEach
    public void setupSerializer() {
        serializer = new MessageWrappingSerializer<>();
    }

    @Test
    public void serialize_shouldWorkAndPrependHeaderAndSignature() throws NoSuchAlgorithmException, InvalidKeyException {
        String testData = "MyTestData";
        serializer.configure(getConfiguration(), false);
        byte[] serialize = serializer.serialize("some.topic", testData);

        String data = new String(Arrays.copyOfRange(serialize, 4 + 32, serialize.length));

        assertEquals(testData, data);
    }

    private Map<String, ?> getConfiguration() throws InvalidKeyException, NoSuchAlgorithmException {
        return Map.of(MessageWrappingSerializer.SERIALIZER_CLASS, MockedDelegateSerializer.class,
                MessageWrappingDeserializer.MESSAGE_WRAPPER, HmacValidationWrapper.createInstance("initKey"));
    }
}
