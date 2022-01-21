package marcono1234.serialization.serialbuilder.codegen.implementation.streamdata;

import marcono1234.serialization.serialbuilder.codegen.implementation.HandleManager;
import marcono1234.serialization.serialbuilder.codegen.implementation.VariableNameManager;
import marcono1234.serialization.serialbuilder.codegen.implementation.writer.CodeWriter;
import marcono1234.serialization.serialbuilder.codegen.implementation.writer.LiteralsHelper;

public record StringObject(
    String value,
    /** Whether this value itself was a handle */
    boolean isHandle
) implements WritableStreamObject {
    @Override
    public void writeCode(CodeWriter writer, HandleManager handleManager, VariableNameManager variableNameManager) {
        if (isHandle) {
            writer.writeUnsupportedHandleUsageComment("String handle");
        }

        writer.writeLine(".string(" + LiteralsHelper.createStringLiteral(value) + ")");
    }
}
