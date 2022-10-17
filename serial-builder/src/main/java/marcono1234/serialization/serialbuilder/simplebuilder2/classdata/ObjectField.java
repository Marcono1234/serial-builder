package marcono1234.serialization.serialbuilder.simplebuilder2.classdata;

import marcono1234.serialization.serialbuilder.builder.implementation.ClassTypeNameHelper;
import marcono1234.serialization.serialbuilder.simplebuilder.api.object.serializable.SerializableObjectData;
import marcono1234.serialization.serialbuilder.simplebuilder2.ObjectBase;
import marcono1234.serialization.serialbuilder.simplebuilder2.SerializedField;

public record ObjectField(
    String fieldName,
    String fieldType,
    ObjectBase object
) implements SerializedField {
    @Override
    public <C> SerializableObjectData<C> buildSerialData(SerializableObjectData<C> start) {
        var objectStart = start.beginObjectField(fieldName, fieldType);
        return object.buildSerialData(objectStart).endField();
    }
}
