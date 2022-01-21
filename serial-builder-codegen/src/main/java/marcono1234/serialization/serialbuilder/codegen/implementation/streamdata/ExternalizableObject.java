package marcono1234.serialization.serialbuilder.codegen.implementation.streamdata;

import marcono1234.serialization.serialbuilder.codegen.implementation.HandleManager;
import marcono1234.serialization.serialbuilder.codegen.implementation.VariableNameManager;
import marcono1234.serialization.serialbuilder.codegen.implementation.writer.CodeWriter;
import marcono1234.serialization.serialbuilder.codegen.implementation.writer.LiteralsHelper;

import java.util.List;

public record ExternalizableObject(
    String className,
    long serialVersionUid,
    /** Data written by {@link java.io.Externalizable#writeExternal(java.io.ObjectOutput)} */
    List<ObjectAnnotationContent> objectAnnotationData,
    boolean usesClassDescHandle,
    /** Whether the descriptor of the class of this object specifies any fields */
    boolean hasFields,
    boolean hasSuperClass,
    HandleManager.Handle<ExternalizableObject> ownHandle
) implements HandleAssignableObject {
    @Override
    public void writeCode(CodeWriter writer, HandleManager handleManager, VariableNameManager variableNameManager) {
        writeData(writer, handleManager, variableNameManager, ".externalizableObject(", false);
    }

    public void writeTopLevelObject(CodeWriter writer, HandleManager handleManager, VariableNameManager variableNameManager) {
        writeData(writer, handleManager, variableNameManager, "byte[] serialData = SimpleSerialBuilder.externalizableObject(", true);
    }

    void writeData(CodeWriter writer, HandleManager handleManager, VariableNameManager variableNameManager, String methodCallString, boolean addTrailingSemicolon) {
        if (usesClassDescHandle) {
            writer.writeUnsupportedHandleUsageComment("Class descriptor");
        }
        if (hasFields) {
            // Note: This is only about fields being declared in the descriptor; Externalizable
            // does not support field values
            writer.writeComment("Fields of class descriptor have been omitted");
        }
        if (hasSuperClass) {
            writer.writeComment("Super class of Externalizable has been omitted");
        }

        // Create copy because lambda creates a new scope
        variableNameManager = variableNameManager.copy();
        String variableName = variableNameManager.getName("writer");

        StringBuilder lineBuilder = new StringBuilder(methodCallString);
        if (ownHandle.isUsed()) {
            lineBuilder.append(handleManager.getHandleName(ownHandle));
            lineBuilder.append(", ");
        }
        lineBuilder.append(LiteralsHelper.createStringLiteral(className));
        lineBuilder.append(", ");
        lineBuilder.append(LiteralsHelper.createLongLiteral(serialVersionUid));
        lineBuilder.append(", ");
        lineBuilder.append(variableName);
        lineBuilder.append(" -> {");
        writer.writeLine(lineBuilder.toString());

        writer.increaseIndentation();
        for (ObjectAnnotationContent data : objectAnnotationData) {
            data.writeCode(writer, handleManager, variableNameManager, variableName);
        }
        writer.decreaseIndentation();
        writer.writeLine("})" + (addTrailingSemicolon ? ";" : ""));
    }
}