package marcono1234.serialization.serialbuilder.simplebuilder2;

import marcono1234.serialization.serialbuilder.builder.api.ThrowingConsumer;
import marcono1234.serialization.serialbuilder.simplebuilder.api.object.ObjectStart;

public record ExternalizableObject(
    String typeName,
    long serialVersionUID,
    ThrowingConsumer<ObjectDataWriter> writer
) implements ProxyInvocationHandlerObject {
    @Override
    public <C> C buildSerialData(ObjectStart<C> objectStart) {
        return objectStart.externalizableObject(typeName, serialVersionUID, delegateWriter -> writer.accept(new ObjectDataWriter(delegateWriter)));
    }
}
