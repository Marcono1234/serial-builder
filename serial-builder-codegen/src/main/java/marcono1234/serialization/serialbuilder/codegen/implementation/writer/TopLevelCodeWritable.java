package marcono1234.serialization.serialbuilder.codegen.implementation.writer;

import marcono1234.serialization.serialbuilder.codegen.implementation.HandleManager;
import marcono1234.serialization.serialbuilder.codegen.implementation.VariableNameManager;

/**
 * A {@link CodeWritable} which can be written as top level object, directly with method
 * invocations on {@code SimpleSerialBuilder}.
 */
public interface TopLevelCodeWritable extends CodeWritable {
    void writeTopLevelCode(CodeWriter writer, HandleManager handleManager, VariableNameManager variableNameManager);
}
