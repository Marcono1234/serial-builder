package marcono1234.serialization.serialbuilder.simplebuilder2;

import marcono1234.serialization.serialbuilder.simplebuilder.api.object.ObjectStart;

// Note: Implementing ProxyInvocationHandlerObject is not completely correct because for example
// a handle to byte[] would not be valid; but solving this in a better way is cumbersome because
// record classes cannot be subclassed
public record ObjectHandle(HandleAssignableObject object) implements ObjectBase, ProxyInvocationHandlerObject {
    @Override
    public <C> C buildSerialData(ObjectStart<C> objectStart) {
        // TODO
        throw new UnsupportedOperationException();
    }
}
