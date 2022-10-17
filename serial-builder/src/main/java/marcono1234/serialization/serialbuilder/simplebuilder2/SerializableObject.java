package marcono1234.serialization.serialbuilder.simplebuilder2;

import marcono1234.serialization.serialbuilder.simplebuilder.api.object.ObjectStart;
import marcono1234.serialization.serialbuilder.simplebuilder.api.object.serializable.SerializableObjectWithDataStart;
import marcono1234.serialization.serialbuilder.simplebuilder2.classdata.ClassData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// TODO: Add type safe subclass for cases where Class is available? Could then be used for object fields to verify correct type
//      Maybe not worth it
public record SerializableObject(List<ClassData> classData) implements ProxyInvocationHandlerObject {
    public SerializableObject {
        if (classData.isEmpty()) {
            throw new IllegalArgumentException("List of class data must not be empty");
        }
    }

    @Override
    public <C> C buildSerialData(ObjectStart<C> objectStart) {
        var start = objectStart.beginSerializableObject();
        SerializableObjectWithDataStart<C> intermediate = null;
        for (ClassData c : classData) {
            intermediate = c.buildSerialData(intermediate == null ? start : intermediate);
        }
        assert intermediate != null;
        return intermediate.endObject();
    }
}
