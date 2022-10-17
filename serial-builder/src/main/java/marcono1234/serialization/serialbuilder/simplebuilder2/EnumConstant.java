package marcono1234.serialization.serialbuilder.simplebuilder2;

import marcono1234.serialization.serialbuilder.simplebuilder.api.object.ObjectStart;

public record EnumConstant(String enumClass, String constantName) implements HandleAssignableObject {
    @Override
    public <C> C buildSerialData(ObjectStart<C> objectStart) {
        return objectStart.enumConstant(enumClass, constantName);
    }
}
