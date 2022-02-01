package marcono1234.serialization.serialbuilder.codegen.implementation.streamdata;

import marcono1234.serialization.serialbuilder.codegen.implementation.HandleManager;
import marcono1234.serialization.serialbuilder.codegen.implementation.VariableNameManager;
import marcono1234.serialization.serialbuilder.codegen.implementation.writer.CodeWriter;

/**
 * Represents the reference to a {@link WritableStreamObject} which does not implement {@link HandleAssignableObject}.
 */
public record WrappedUnassignableObject(
    WritableStreamObject object,
    /** Describes the type of {@link #object} in a human-readable way */
    String typeDescription
) implements WritableStreamObject {
    @Override
    public void writeCode(CodeWriter writer, HandleManager handleManager, VariableNameManager variableNameManager) {
        writer.writeUnsupportedHandleUsageComment(typeDescription);
        object.writeCode(writer, handleManager, variableNameManager);
    }
}
