package marcono1234.serialization.serialbuilder.simplebuilder2.array;

import marcono1234.serialization.serialbuilder.simplebuilder.api.object.ObjectStart;
import marcono1234.serialization.serialbuilder.simplebuilder2.HandleAssignableObject;
import marcono1234.serialization.serialbuilder.simplebuilder2.ObjectBase;

public record LongArray(long[] array) implements HandleAssignableObject {
    @Override
    public <C> C buildSerialData(ObjectStart<C> objectStart) {
        return objectStart.array(array);
    }
}
