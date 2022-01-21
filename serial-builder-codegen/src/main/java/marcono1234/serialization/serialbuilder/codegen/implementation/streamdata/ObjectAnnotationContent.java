package marcono1234.serialization.serialbuilder.codegen.implementation.streamdata;

import marcono1234.serialization.serialbuilder.codegen.implementation.HandleManager;
import marcono1234.serialization.serialbuilder.codegen.implementation.VariableNameManager;
import marcono1234.serialization.serialbuilder.codegen.implementation.streamdata.array.PrimitiveArrayHelper;
import marcono1234.serialization.serialbuilder.codegen.implementation.writer.CodeWriter;

import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

/**
 * "Object annotation" content, as defined by the serialization format grammar. This represents the data written by
 * {@link java.io.Externalizable#writeExternal(ObjectOutput)} and the special {@code writeObject} method.
 */
public sealed interface ObjectAnnotationContent {
    record ObjectObjectAnnotationContent(WritableStreamObject object) implements ObjectAnnotationContent {
        @Override
        public void writeCode(CodeWriter writer, HandleManager handleManager, VariableNameManager variableNameManager, String writerVariableName) {
            // Write code to a separate writer and then insert code as call on writer variable
            // Collect leading comments in separate list to avoid them appearing between writer variable and method call
            List<String> leadingComments = new ArrayList<>();
            CodeWriter objectCodeWriter = new CodeWriter(writer) {
                private boolean wroteLine = false;

                @Override
                public void writeLine(String line) {
                    super.writeLine(line);
                    wroteLine = true;
                }

                @Override
                public void writeComment(String comment) {
                    if (wroteLine) {
                        super.writeComment(comment);
                    } else {
                        leadingComments.add(comment);
                    }
                }
            };
            object.writeCode(objectCodeWriter, handleManager, variableNameManager);

            // Write collected comments (if any)
            leadingComments.forEach(writer::writeComment);

            // Call getCode(false) to skip indentation check (because indentation is > 0)
            // Call strip() to remove leading and trailing whitespace to be able to insert code into line
            String objectCode = objectCodeWriter.getCode(false).strip();
            writer.writeLine(writerVariableName + objectCode + ";");
        }
    }

    record BinaryObjectAnnotationContent(byte[] bytes) implements ObjectAnnotationContent {
        @Override
        public void writeCode(CodeWriter writer, HandleManager handleManager, VariableNameManager variableNameManager, String writerVariableName) {
            String byteArrayString = PrimitiveArrayHelper.createArrayCode(bytes);
            writer.writeLine(writerVariableName + ".write(" + byteArrayString + ");");
        }
    }

    /**
     * Generates the code representing this data. The `writerVariableName` is the name of the
     * {@code marcono1234.serialization.serialbuilder.simplebuilder.api.ObjectBuildingDataOutput} variable on which the
     * writing calls in the generated code should be performed.
     */
    void writeCode(CodeWriter writer, HandleManager handleManager, VariableNameManager variableNameManager, String writerVariableName);
}
