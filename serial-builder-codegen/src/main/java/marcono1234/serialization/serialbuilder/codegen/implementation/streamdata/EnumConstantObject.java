package marcono1234.serialization.serialbuilder.codegen.implementation.streamdata;

import marcono1234.serialization.serialbuilder.codegen.implementation.HandleManager;
import marcono1234.serialization.serialbuilder.codegen.implementation.VariableNameManager;
import marcono1234.serialization.serialbuilder.codegen.implementation.writer.CodeWriter;
import marcono1234.serialization.serialbuilder.codegen.implementation.writer.LiteralsHelper;

public record EnumConstantObject(
    String enumType,
    String constantName,
    /** Whether the serialization data did not include the descriptor for {@code java.lang.Enum} */
    boolean isMissingSuperType,
    /** Whether any of the data of the constant uses a handle, e.g. the constant name or class descriptor */
    boolean usesHandle
) implements WritableStreamObject {
    @Override
    public void writeCode(CodeWriter writer, HandleManager handleManager, VariableNameManager variableNameManager) {
        if (usesHandle) {
            writer.writeUnsupportedHandleUsageComment("Enum class descriptor or constant name");
        }
        if (isMissingSuperType) {
            writer.writeComment("Serial data did not include descriptor for java.lang.Enum");
        }

        writer.writeLine(".enumConstant(" + LiteralsHelper.createStringLiteral(enumType) + ", " + LiteralsHelper.createStringLiteral(constantName) + ')');
    }
}
