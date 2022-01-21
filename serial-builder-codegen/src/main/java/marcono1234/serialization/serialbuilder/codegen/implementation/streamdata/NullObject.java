package marcono1234.serialization.serialbuilder.codegen.implementation.streamdata;

import marcono1234.serialization.serialbuilder.codegen.implementation.HandleManager;
import marcono1234.serialization.serialbuilder.codegen.implementation.VariableNameManager;
import marcono1234.serialization.serialbuilder.codegen.implementation.writer.CodeWriter;

/**
 * {@code null} reference in the serialization data.
 */
public class NullObject implements WritableStreamObject {
    public static final NullObject INSTANCE = new NullObject();

    private NullObject() {
    }

    @Override
    public void writeCode(CodeWriter writer, HandleManager handleManager, VariableNameManager variableNameManager) {
        writer.writeLine(".nullObject()");
    }
}
