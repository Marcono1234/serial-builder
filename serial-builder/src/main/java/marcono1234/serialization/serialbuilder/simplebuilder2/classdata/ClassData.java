package marcono1234.serialization.serialbuilder.simplebuilder2.classdata;

import marcono1234.serialization.serialbuilder.builder.api.ThrowingConsumer;
import marcono1234.serialization.serialbuilder.simplebuilder.api.object.serializable.SerializableObjectStart;
import marcono1234.serialization.serialbuilder.simplebuilder.api.object.serializable.SerializableObjectWithDataStart;
import marcono1234.serialization.serialbuilder.simplebuilder2.ObjectDataWriter;
import marcono1234.serialization.serialbuilder.simplebuilder2.SerializedField;

import java.util.List;

public record ClassData(
    String className,
    long serialVersion,
    List<SerializedField> fields,
    ThrowingConsumer<ObjectDataWriter> dataWriter
) {
    public <C> SerializableObjectWithDataStart<C> buildSerialData(SerializableObjectStart<C> start) {
        var classDataStart = start.beginClassData(className, serialVersion);
        for (SerializedField field : fields) {
            classDataStart = field.buildSerialData(classDataStart);
        }

        if (dataWriter != null) {
            return classDataStart.writeObjectWith(delegateWriter -> dataWriter.accept(new ObjectDataWriter(delegateWriter))).endClassData();
        } else {
            return classDataStart.endClassData();
        }
    }
}
