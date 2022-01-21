package marcono1234.serialization.serialbuilder.codegen.implementation.streamdata;

import marcono1234.serialization.serialbuilder.codegen.implementation.HandleManager;
import marcono1234.serialization.serialbuilder.codegen.implementation.VariableNameManager;
import marcono1234.serialization.serialbuilder.codegen.implementation.writer.CodeWriter;
import marcono1234.serialization.serialbuilder.codegen.implementation.writer.LiteralsHelper;

import java.util.List;
import java.util.Optional;

public record SerializableObject(List<SerializableClassData> classDataList, HandleManager.Handle<SerializableObject> ownHandle) implements HandleAssignableObject {
    public record SerializableClassData(
        String className,
        long serialVersionUid,
        List<PrimitiveFieldValue> primitiveFieldValues,
        List<ObjectFieldValue> objectFieldValues,
        /** Data written by {@code writeObject}, if any */
        // Use Optional to differentiate between no `writeObject` method, and no data being written
        Optional<List<ObjectAnnotationContent>> objectAnnotationData,
        boolean usesClassDescHandle
    ) {
        public record PrimitiveFieldValue(String fieldName, Object primitiveValue) {
        }

        public record ObjectFieldValue(String fieldName, String fieldType, WritableStreamObject value, boolean usesFieldTypeHandle) {
        }
    }

    @Override
    public void writeCode(CodeWriter writer, HandleManager handleManager, VariableNameManager variableNameManager) {
        writeData(writer, handleManager, variableNameManager, ".beginSerializableObject(", false);
    }

    public void writeTopLevelObject(CodeWriter writer, HandleManager handleManager, VariableNameManager variableNameManager) {
        writeData(writer, handleManager, variableNameManager, "byte[] serialData = SimpleSerialBuilder.startSerializableObject(", true);
    }

    void writeData(CodeWriter writer, HandleManager handleManager, VariableNameManager variableNameManager, String methodCallString, boolean addTrailingSemicolon) {
        StringBuilder lineBuilder = new StringBuilder(methodCallString);
        if (ownHandle.isUsed()) {
            lineBuilder.append(handleManager.getHandleName(ownHandle));
        }
        lineBuilder.append(')');
        writer.writeLine(lineBuilder.toString());
        writer.increaseIndentation();
        writeClassData(writer, handleManager, variableNameManager);
        writer.decreaseIndentation();
        writer.writeLine(".endObject()" + (addTrailingSemicolon ? ";" : ""));
    }

    private void writeClassData(CodeWriter writer, HandleManager handleManager, VariableNameManager variableNameManager) {
        for (SerializableClassData classData : classDataList) {
            if (classData.usesClassDescHandle()) {
                writer.writeUnsupportedHandleUsageComment("Class descriptor");
            }
            writer.writeLine(".beginClassData(" + LiteralsHelper.createStringLiteral(classData.className()) + ", " + LiteralsHelper.createLongLiteral(classData.serialVersionUid()) + ')');
            writer.increaseIndentation();

            for (SerializableClassData.PrimitiveFieldValue primitiveFieldValue : classData.primitiveFieldValues()) {
                Object fieldValue = primitiveFieldValue.primitiveValue();
                Class<?> fieldValueType = fieldValue.getClass();
                String valueString = LiteralsHelper.primitiveToString(fieldValue);

                StringBuilder lineBuilder = new StringBuilder(".primitiveField(");
                lineBuilder.append(LiteralsHelper.createStringLiteral(primitiveFieldValue.fieldName()));
                lineBuilder.append(", ");
                // Add cast where otherwise wrong overload would be used
                if (fieldValueType == Byte.class) {
                    lineBuilder.append("(byte) ");
                } else if (fieldValueType == Short.class) {
                    lineBuilder.append("(short) ");
                }
                lineBuilder.append(valueString);
                lineBuilder.append(')');

                writer.writeLine(lineBuilder.toString());
            }

            for (SerializableClassData.ObjectFieldValue objectFieldValue : classData.objectFieldValues()) {
                if (objectFieldValue.usesFieldTypeHandle()) {
                    writer.writeUnsupportedHandleUsageComment("Object field type name");
                }
                writer.writeLine(".beginObjectField(" + LiteralsHelper.createStringLiteral(objectFieldValue.fieldName()) + ", " + LiteralsHelper.createStringLiteral(objectFieldValue.fieldType()) + ')');
                writer.increaseIndentation();
                objectFieldValue.value().writeCode(writer, handleManager, variableNameManager);
                writer.decreaseIndentation();
                writer.writeLine(".endField()");
            }

            Optional<List<ObjectAnnotationContent>> optObjectAnnotationData = classData.objectAnnotationData();
            if (optObjectAnnotationData.isPresent()) {
                // Create copy because lambda creates a new scope
                variableNameManager = variableNameManager.copy();
                String variableName = variableNameManager.getName("writer");

                writer.writeLine(".writeObjectWith(" + variableName + " -> {");
                writer.increaseIndentation();
                for (ObjectAnnotationContent data : optObjectAnnotationData.get()) {
                    data.writeCode(writer, handleManager, variableNameManager, variableName);
                }
                writer.decreaseIndentation();
                writer.writeLine("})");
            }

            writer.decreaseIndentation();
            writer.writeLine(".endClassData()");
        }
    }
}
