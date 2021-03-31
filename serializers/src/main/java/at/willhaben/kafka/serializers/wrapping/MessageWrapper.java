package at.willhaben.kafka.serializers.wrapping;

/**
 * Wraps/unwraps a raw message into a different format. Even though this two steps are usually done in different
 * systems (wrap in producer, unwrap in consumer), they are logically dependent and therefore implemented
 * in the same interface. An implementation should satisfy the condition unwrapMessage(wrapMessage(M)) = M
 * (implied that potential configuration is compatible).
 */
public interface MessageWrapper {

    byte[] unwrapMessage(byte[] data);

    byte[] wrapMessage(byte[] data);
}
