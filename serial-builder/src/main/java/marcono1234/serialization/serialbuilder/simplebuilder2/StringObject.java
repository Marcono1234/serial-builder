package marcono1234.serialization.serialbuilder.simplebuilder2;

import marcono1234.serialization.serialbuilder.simplebuilder.api.object.ObjectStart;

public record StringObject(String s) implements HandleAssignableObject {
    @Override
    public <C> C buildSerialData(ObjectStart<C> objectStart) {
        return objectStart.string(s);
    }
}
