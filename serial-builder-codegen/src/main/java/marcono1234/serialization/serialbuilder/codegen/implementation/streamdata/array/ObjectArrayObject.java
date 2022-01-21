package marcono1234.serialization.serialbuilder.codegen.implementation.streamdata.array;

import marcono1234.serialization.serialbuilder.codegen.implementation.HandleManager;
import marcono1234.serialization.serialbuilder.codegen.implementation.VariableNameManager;
import marcono1234.serialization.serialbuilder.codegen.implementation.streamdata.WritableStreamObject;
import marcono1234.serialization.serialbuilder.codegen.implementation.writer.CodeWriter;
import marcono1234.serialization.serialbuilder.codegen.implementation.writer.LiteralsHelper;

import java.util.List;

public record ObjectArrayObject(
    /**
     * Name of the array type in the form returned by {@link Class#getTypeName()}
     */
    String arrayType,
    List<WritableStreamObject> elements,
    boolean usesDescriptorHandle,
    HandleManager.Handle<ArrayObject> ownHandle
) implements ArrayObject {
    @Override
    public void writeCode(CodeWriter writer, HandleManager handleManager, VariableNameManager variableNameManager) {
        if (usesDescriptorHandle) {
            writer.writeUnsupportedHandleUsageComment("Array type descriptor");
        }

        StringBuilder beginStringBuilder = new StringBuilder();
        beginStringBuilder.append(".beginObjectArray(");
        if (ownHandle.isUsed()) {
            beginStringBuilder.append(handleManager.getHandleName(ownHandle));
            beginStringBuilder.append(", ");
        }
        beginStringBuilder.append(LiteralsHelper.createStringLiteral(arrayType));
        beginStringBuilder.append(")");
        writer.writeLine(beginStringBuilder.toString());

        writer.increaseIndentation();
        elements.forEach(e -> e.writeCode(writer, handleManager, variableNameManager));
        writer.decreaseIndentation();

        writer.writeLine(".endArray()");
    }
}
