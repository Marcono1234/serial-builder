package marcono1234.serialization.serialbuilder.codegen.implementation.streamdata;

import marcono1234.serialization.serialbuilder.codegen.implementation.HandleManager;
import marcono1234.serialization.serialbuilder.codegen.implementation.VariableNameManager;
import marcono1234.serialization.serialbuilder.codegen.implementation.writer.CodeWriter;

public record HandleObject(HandleManager.Handle<? extends WritableStreamObject> handle) implements WritableStreamObject {
    public HandleObject {
        handle.markUsed();
    }

    @Override
    public void writeCode(CodeWriter writer, HandleManager handleManager, VariableNameManager variableNameManager) {
        writer.writeLine(".objectHandle(" + handleManager.getHandleName(handle) + ')');
    }
}
