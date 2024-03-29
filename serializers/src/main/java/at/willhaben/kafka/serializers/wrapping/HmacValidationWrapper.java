package at.willhaben.kafka.serializers.wrapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Calculates a HMAC hash for the message and prepends it to the actual output. When unwrapping, the hash is calculated
 * again for the actual message and validated towards the received hash. This implies that the wrapping and
 * unwrapping instances are initiated using the same key. For validation, multiple HMACs can be defined, to support
 * UC like a separate HMAC for each sender, or to allow transitioning to a new HMAC in production systems without
 * invalidating unprocessed messages.
 *
 * The Wrapper can be configured to allow unvalidated messages which can be useful for transitioning an already existing
 * topic. However this should only be used for a limited time, because otherwise it would defeat the purpose of
 * the whole wrapping mechanism.
 */
public class HmacValidationWrapper implements MessageWrapper {
    private static final Logger logger = LoggerFactory.getLogger(HmacValidationWrapper.class);
    private static final byte[] IDENTIFYING_HEADER = new byte[] {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA};
    private static final int HEADER_SIZE_BYTES = 4;
    private static final String ALGORITHM = "HmacSHA256";
    private static final int SIGNATURE_SIZE_BYTES = 256 / 8;

    private final Mac primaryMac;
    private final Collection<Mac> allValidMacs;
    private final boolean allowUnvalidatedMessages;

    private boolean validationEnabled = true;

    private HmacValidationWrapper(Mac primaryMac, Collection<Mac> additionalMacs, boolean allowUnvalidatedMessages) {
        this.primaryMac = primaryMac;
        this.allowUnvalidatedMessages = allowUnvalidatedMessages;

        this.allValidMacs = new HashSet<>();
        this.allValidMacs.add(primaryMac);
        this.allValidMacs.addAll(additionalMacs);
    }

    public static MessageWrapper createInstance(String key) {
        return createInstance(key, false);
    }

    public static MessageWrapper createInstance(String key, boolean allowUnvalidatedMessages) {
        return createInstance(key, Collections.emptyList(), allowUnvalidatedMessages);
    }

    public static MessageWrapper createInstance(String key, Collection<String> additionalKeys, boolean allowUnvalidatedMessages) {
        Mac primaryMac = createMacInstance(key);

        Set<Mac> additionalMacs = additionalKeys.stream()
                .map(HmacValidationWrapper::createMacInstance)
                .collect(Collectors.toSet());

        return new HmacValidationWrapper(primaryMac, additionalMacs, allowUnvalidatedMessages);
    }

    private static Mac createMacInstance(String key) {
        SecretKeySpec spec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), ALGORITHM);

        Mac mac = null;
        try {
            mac = Mac.getInstance(ALGORITHM);
            mac.init(spec);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalArgumentException("Error when instantiating the message wrapper", e);
        }

        return mac;
    }

    public boolean isValidationEnabled() {
        return validationEnabled;
    }

    public void setValidationEnabled(boolean validationEnabled) {
        this.validationEnabled = validationEnabled;
    }

    @Override
    public byte[] unwrapMessage(byte[] rawData) {
        byte[] header = Arrays.copyOfRange(rawData, 0, HEADER_SIZE_BYTES);

        if (isHeaderSet(header)) {
            return validateAndExtractData(rawData);
        } else {
            if (!validationEnabled || allowUnvalidatedMessages) {
                logger.info("Unvalidated message has been processed");
                return rawData;
            } else {
                throw new MessageWrapperException("Header missing, message could not be validated");
            }
        }
    }

    private boolean isHeaderSet(byte[] receivedHeader) {
        return Arrays.compare(receivedHeader, IDENTIFYING_HEADER) == 0;
    }

    private byte[] validateAndExtractData(byte[] rawData) throws MessageWrapperException {
        byte[] receivedSignature = Arrays.copyOfRange(rawData, HEADER_SIZE_BYTES, HEADER_SIZE_BYTES + SIGNATURE_SIZE_BYTES);
        byte[] data = Arrays.copyOfRange(rawData, HEADER_SIZE_BYTES + SIGNATURE_SIZE_BYTES, rawData.length);

        if (!validationEnabled || allValidMacs.stream().anyMatch(mac -> this.validate(mac, data, receivedSignature))) {
            return data;
        }

        throw new MessageWrapperException("Signature validation failed");
    }

    private boolean validate(Mac mac, byte[] data, byte[] receivedSignature) {
        byte[] calculatedSignature = mac.doFinal(data);
        final boolean isValid = Arrays.compare(receivedSignature, calculatedSignature) == 0;
        if (!isValid) {
            logger.debug("Hex signature: {} does not match with calculated hex value: {}",
                    byteArrayToHex(receivedSignature),
                    byteArrayToHex(calculatedSignature));
        }
        return isValid;
    }

    private static String byteArrayToHex(byte[] array) {
        StringBuilder sb = new StringBuilder(array.length * 2);
        for (byte b : array) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    @Override
    public byte[] wrapMessage(byte[] data) {
        byte[] signature = primaryMac.doFinal(data);
        return combineArrays(signature, data);
    }

    private byte[] combineArrays(byte[] signature, byte[] data) {
        final int headerLength = IDENTIFYING_HEADER.length;
        byte[] combined = new byte[headerLength + signature.length + data.length];

        System.arraycopy(IDENTIFYING_HEADER, 0, combined, 0, headerLength);
        System.arraycopy(signature, 0, combined, headerLength, signature.length);
        System.arraycopy(data, 0, combined, headerLength + signature.length, data.length);
        return combined;
    }
}
