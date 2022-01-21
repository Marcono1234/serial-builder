package marcono1234.serialization.serialbuilder.codegen.implementation.writer;

import marcono1234.serialization.serialbuilder.codegen.implementation.HandleManager;
import marcono1234.serialization.serialbuilder.codegen.implementation.VariableNameManager;

/**
 * Represents an object which can be written to the generated code.
 */
public interface CodeWritable {
    void writeCode(CodeWriter writer, HandleManager handleManager, VariableNameManager variableNameManager);
}
