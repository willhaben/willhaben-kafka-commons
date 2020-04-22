# Willhaben custom de/serializers

A collection of de/serializers for kafka streams, consumers and producers.

## Delegating de/serializer

An abstract implementation that takes another de/serializer as parameter and provides hook methods to run code before and after de/serialization.

## Message wrapping de/serializer

An implementation of the delegating de/serializer that takes allows to manipulate the message after serialization and before deserialization e.g. to encrypt the message, append metadata, or any other usercase you can imagine. To use it you have to provide an implementation of a MessageWrapper object.

### The HmacValidationWrapper

An implementation of a message wrapper that prepends an 256 byte long HMAC to the message during serialization and validates this HMAC during deserialization. To make sure this works properly the secret key must be identical. Key exchange is not part of this class, the Key has to be provided when initializing the wrapper class.
