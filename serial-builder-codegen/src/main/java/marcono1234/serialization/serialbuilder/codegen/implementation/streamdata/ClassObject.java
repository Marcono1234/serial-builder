package marcono1234.serialization.serialbuilder.codegen.implementation.streamdata;

import marcono1234.serialization.serialbuilder.codegen.implementation.HandleManager;
import marcono1234.serialization.serialbuilder.codegen.implementation.VariableNameManager;
import marcono1234.serialization.serialbuilder.codegen.implementation.streamdata.descriptor.DescriptorStreamObject;
import marcono1234.serialization.serialbuilder.codegen.implementation.streamdata.descriptor.NonProxyDescriptorData;
import marcono1234.serialization.serialbuilder.codegen.implementation.streamdata.descriptor.ProxyDescriptorData;
import marcono1234.serialization.serialbuilder.codegen.implementation.writer.CodeWriter;
import marcono1234.serialization.serialbuilder.codegen.implementation.writer.LiteralsHelper;

import java.util.Iterator;

import static java.io.ObjectStreamConstants.SC_ENUM;
import static java.io.ObjectStreamConstants.SC_EXTERNALIZABLE;
import static java.io.ObjectStreamConstants.SC_SERIALIZABLE;

public record ClassObject(DescriptorStreamObject descriptorStreamObject, boolean isDescriptorHandle) implements WritableStreamObject {
    @Override
    public void writeCode(CodeWriter writer, HandleManager handleManager, VariableNameManager variableNameManager) {
        if (isDescriptorHandle) {
            writer.writeUnsupportedHandleUsageComment("Class descriptor is handle");
        }

        StringBuilder lineBuilder = new StringBuilder();

        if (descriptorStreamObject instanceof ProxyDescriptorData proxyClassDesc) {
            if (!isDescriptorHandle && proxyClassDesc.usesAnyHandle()) {
                writer.writeUnsupportedHandleUsageComment("Class descriptor uses handle");
            }

            // Builder methods do not support setting most descriptor data (e.g. custom flags, fields, or supertypes)
            writer.writeComment("Code might not create identical class descriptor (though usually does not matter)");

            lineBuilder.append(".proxyClass(");
            Iterator<String> namesIterator = proxyClassDesc.interfaceNames().iterator();

            while (namesIterator.hasNext()) {
                lineBuilder.append(LiteralsHelper.createStringLiteral(namesIterator.next()));

                if (namesIterator.hasNext()) {
                    lineBuilder.append(", ");
                }
            }

            lineBuilder.append(')');
        } else if (descriptorStreamObject instanceof NonProxyDescriptorData nonProxyClassDesc) {
            if (!isDescriptorHandle && nonProxyClassDesc.usesAnyHandle()) {
                writer.writeUnsupportedHandleUsageComment("Class descriptor uses handle");
            }

            // Builder methods do not support setting most descriptor data (e.g. custom flags, fields, or supertypes)
            writer.writeComment("Code might not create identical class descriptor (though usually does not matter)");

            String typeName = nonProxyClassDesc.typeName();
            long serialVersionUid = nonProxyClassDesc.serialVersionUid();
            byte flags = nonProxyClassDesc.flags();

            if ((flags & SC_ENUM) != 0) {
                lineBuilder.append(".enumClass(")
                    .append(LiteralsHelper.createStringLiteral(typeName))
                    .append(')');
            } else if (typeName.endsWith("[]")) {
                lineBuilder.append(".arrayClass(")
                    .append(LiteralsHelper.createStringLiteral(typeName))
                    .append(')');
            } else if ((flags & SC_EXTERNALIZABLE) != 0) {
                lineBuilder.append(".externalizableClass(")
                    .append(LiteralsHelper.createStringLiteral(typeName))
                    .append(", ")
                    .append(LiteralsHelper.createLongLiteral(serialVersionUid))
                    .append(')');
            } else if ((flags & SC_SERIALIZABLE) != 0) {
                lineBuilder.append(".serializableClass(")
                    .append(LiteralsHelper.createStringLiteral(typeName))
                    .append(", ")
                    .append(LiteralsHelper.createLongLiteral(serialVersionUid))
                    .append(')');
            } else {
                lineBuilder.append(".nonSerializableClass(")
                    .append(LiteralsHelper.createStringLiteral(typeName))
                    .append(')');
            }
        } else {
            // All possible DescriptorStreamObject subtypes have been covered above
            throw new AssertionError("Unexpected class descriptor type: " + descriptorStreamObject);
        }

        writer.writeLine(lineBuilder.toString());
    }
}
