package marcono1234.serialization.serialbuilder.codegen.implementation.streamdata.array;

import marcono1234.serialization.serialbuilder.codegen.implementation.HandleManager;
import marcono1234.serialization.serialbuilder.codegen.implementation.VariableNameManager;
import marcono1234.serialization.serialbuilder.codegen.implementation.writer.CodeWriter;

public record PrimitiveArrayObject(Object array, boolean usesDescriptorHandle, HandleManager.Handle<ArrayObject> ownHandle) implements ArrayObject {
    @Override
    public void writeCode(CodeWriter writer, HandleManager handleManager, VariableNameManager variableNameManager) {
        if (usesDescriptorHandle) {
            writer.writeUnsupportedHandleUsageComment("Array type descriptor");
        }

        StringBuilder lineBuilder = new StringBuilder(".array(");
        if (ownHandle.isUsed()) {
            lineBuilder.append(handleManager.getHandleName(ownHandle));
            lineBuilder.append(", ");
        }
        lineBuilder.append(PrimitiveArrayHelper.createArrayCode(array));
        lineBuilder.append(')');
        writer.writeLine(lineBuilder.toString());
    }
}
