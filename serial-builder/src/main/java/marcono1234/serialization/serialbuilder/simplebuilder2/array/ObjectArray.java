package marcono1234.serialization.serialbuilder.simplebuilder2.array;

import marcono1234.serialization.serialbuilder.simplebuilder.api.object.ObjectStart;
import marcono1234.serialization.serialbuilder.simplebuilder2.HandleAssignableObject;
import marcono1234.serialization.serialbuilder.simplebuilder2.ObjectBase;

import java.util.List;

public record ObjectArray(String arrayType, List<ObjectBase> elements) implements HandleAssignableObject {
    @Override
    public <C> C buildSerialData(ObjectStart<C> objectStart) {
        var start = objectStart.beginObjectArray(arrayType);
        for (ObjectBase element : elements) {
            start = element.buildSerialData(start);
        }
        return start.endArray();
    }
}
