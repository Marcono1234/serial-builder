package marcono1234.serialization.serialbuilder.simplebuilder2;

import marcono1234.serialization.serialbuilder.SimpleSerialBuilder;
import marcono1234.serialization.serialbuilder.simplebuilder.api.object.ObjectStart;

// TODO Better class name?
public sealed interface ObjectBase permits HandleAssignableObject, NullObject, ObjectHandle {
    <C> C buildSerialData(ObjectStart<C> objectStart);

    default byte[] createSerialData() {
        return SimpleSerialBuilder.writeSerializationDataWith(this::buildSerialData);
    }
}
