package marcono1234.serialization.serialbuilder.simplebuilder2;

import marcono1234.serialization.serialbuilder.simplebuilder.api.object.serializable.SerializableObjectData;
import marcono1234.serialization.serialbuilder.simplebuilder2.classdata.ObjectField;
import marcono1234.serialization.serialbuilder.simplebuilder2.classdata.PrimitiveField;

public sealed interface SerializedField permits PrimitiveField, ObjectField {
    <C> SerializableObjectData<C> buildSerialData(SerializableObjectData<C> start);
}
