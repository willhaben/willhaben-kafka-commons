package at.willhaben.kafka.serializers;

import at.willhaben.kafka.serializers.helpers.MockedDelegateDeserializer;
import at.willhaben.kafka.serializers.wrapping.HmacValidationWrapper;
import at.willhaben.kafka.serializers.wrapping.MessageWrapperException;
import at.willhaben.kafka.serializers.wrapping.MessageWrappingDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ValidatingDeserializerTest {

    private static final String INVALID_MESSAGE_BASE64="AgfuH1IK46u2/TW8J8p+dK3YmWpw7fLm5QhGU3Tw+QJNeVRlc3REYXRh";
    private static final String VALID_MESSAGE_BASE64 = "qqqqqgIH7h9SCuOrtvw1vCfKfnSt2JlqcO3y5uUIRlN08PkCTXlUZXN0RGF0YQ==";
    private static final String INVALID_SIGNATURE_BASE64 = "qqqqqgFH7h9SCuOrtvw1vCfKfnSt2JlqcO3y5uUIRlN08PkCTXlUZXN0RGF0YQ==";
    private static final String VALID_MESSAGE_KEY = "initKey";
    private static final String VALID_MESSAGE_CONTENT = "MyTestData";

    private MessageWrappingDeserializer<String> deserializer;

    @BeforeEach
    public void setupDeserializer() {
        deserializer = new MessageWrappingDeserializer<>();
    }

    @Test
    public void deserialize_shouldWork() {
        deserializer.configure(getConfiguration(VALID_MESSAGE_KEY), false);
        Object deserialize = deserializer.deserialize("some.topic", getMessageAsBytes(VALID_MESSAGE_BASE64));

        assertEquals(deserialize, VALID_MESSAGE_CONTENT);
    }

    @Test
    public void deserializeUnwrappedMessageWithAllowedModeOn_shouldWork() {
        String someMessage = "Some random message";
        deserializer.configure(getBackwardsCompatibleConfiguration(VALID_MESSAGE_KEY), false);
        Object deserialize = deserializer.deserialize("some.topic", someMessage.getBytes());

        assertEquals(deserialize, someMessage);
    }

    @Test
    public void deserializeUnwrappedMessageWithAllowedModeOff_shouldThrowException() {
        String someMessage = "Some random message";
        deserializer.configure(getConfiguration(VALID_MESSAGE_KEY), false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> deserializer.deserialize("some.topic", someMessage.getBytes())
        );

        assertTrue(exception.getCause() instanceof MessageWrapperException);
    }

    @Test
    public void deserializeWithTemperedMessage_shouldThrowException() {
        deserializer.configure(getConfiguration("wrongKey"), false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> deserializer.deserialize("some.topic", getMessageAsBytes(INVALID_MESSAGE_BASE64))
        );

        assertTrue(exception.getCause() instanceof MessageWrapperException);
    }

    @Test
    public void deserializeWithTemperedSignature_shouldThrowException() {
        deserializer.configure(getConfiguration(VALID_MESSAGE_KEY), false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> deserializer.deserialize("some.topic", getMessageAsBytes(INVALID_SIGNATURE_BASE64))
        );

        assertTrue(exception.getCause() instanceof MessageWrapperException);
    }

    @Test
    public void deserializeWithWrongKey_shouldThrowException() {
        deserializer.configure(getConfiguration("wrongKey"), false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> deserializer.deserialize("some.topic", getMessageAsBytes(VALID_MESSAGE_BASE64))
        );

        assertTrue(exception.getCause() instanceof MessageWrapperException);
    }

    private byte[] getMessageAsBytes(String message) {
        return Base64.getDecoder().decode(message);
    }

    private Map<String, ?> getConfiguration(String hmacKey) {
        return Map.of(MessageWrappingDeserializer.DESERIALIZER_CLASS, MockedDelegateDeserializer.class,
                MessageWrappingDeserializer.MESSAGE_WRAPPER, HmacValidationWrapper.createInstance(hmacKey));
    }

    private Map<String, ?> getBackwardsCompatibleConfiguration(String hmacKey) {
        return Map.of(MessageWrappingDeserializer.DESERIALIZER_CLASS, MockedDelegateDeserializer.class,
                MessageWrappingDeserializer.MESSAGE_WRAPPER, HmacValidationWrapper.createInstance(hmacKey, true));
    }
}
