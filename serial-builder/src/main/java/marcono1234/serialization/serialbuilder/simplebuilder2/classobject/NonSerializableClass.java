package marcono1234.serialization.serialbuilder.simplebuilder2.classobject;

import marcono1234.serialization.serialbuilder.simplebuilder.api.object.ObjectStart;
import marcono1234.serialization.serialbuilder.simplebuilder2.HandleAssignableObject;

public record NonSerializableClass(String className) implements ClassObject {
    @Override
    public <C> C buildSerialData(ObjectStart<C> objectStart) {
        return objectStart.nonSerializableClass(className);
    }
}
