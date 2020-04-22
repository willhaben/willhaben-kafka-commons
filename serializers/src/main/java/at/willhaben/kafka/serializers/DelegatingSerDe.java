package at.willhaben.kafka.serializers;

import java.lang.reflect.InvocationTargetException;

public abstract class DelegatingSerDe {

    protected <U> U createDelegate(Class<U> delegateClass, Class<?> expectedType) {
        if (delegateClass != null && expectedType.isAssignableFrom(delegateClass)) {
            try {
                return delegateClass.getConstructor().newInstance();
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("Delegating object must have a default constructor", e);
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
                throw new IllegalStateException("Constructor could not be invoked", e);
            }
        }

        throw new IllegalArgumentException("Delegate type must be defined and assignable to type " + expectedType.getName());
    }
}
