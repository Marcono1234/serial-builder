package marcono1234.serialization.serialbuilder.codegen.implementation.streamdata;

import marcono1234.serialization.serialbuilder.codegen.implementation.HandleManager;
import marcono1234.serialization.serialbuilder.codegen.implementation.SerialDataCodeGen;
import marcono1234.serialization.serialbuilder.codegen.implementation.VariableNameManager;
import marcono1234.serialization.serialbuilder.codegen.implementation.writer.CodeWriter;
import marcono1234.serialization.serialbuilder.codegen.implementation.writer.LiteralsHelper;
import marcono1234.serialization.serialbuilder.codegen.implementation.writer.TopLevelCodeWritable;

import java.util.Iterator;
import java.util.List;

public record ProxyObject(
    List<String> interfaceNames,
    WritableStreamObject invocationHandler,
    boolean usesClassDescHandle,
    HandleManager.Handle<ProxyObject> ownHandle
) implements HandleAssignableObject, TopLevelCodeWritable {

    @Override
    public void writeCode(CodeWriter writer, HandleManager handleManager, VariableNameManager variableNameManager) {
        writeData(writer, handleManager, variableNameManager, ".beginProxyObject(", false);
    }

    @Override
    public void writeTopLevelCode(CodeWriter writer, HandleManager handleManager, VariableNameManager variableNameManager) {
        writeData(writer, handleManager, variableNameManager, SerialDataCodeGen.GENERATED_SERIAL_DATA_VARIABLE + "SimpleSerialBuilder.startProxyObject(", true);
    }

    void writeData(CodeWriter writer, HandleManager handleManager, VariableNameManager variableNameManager, String methodCallString, boolean addTrailingSemicolon) {
        writeStart(writer, handleManager, methodCallString);
        writer.increaseIndentation();
        writeInvocationHandler(writer, handleManager, variableNameManager);
        writer.decreaseIndentation();
        writer.writeLine(".endProxyObject()" + (addTrailingSemicolon ? ";" : ""));
    }

    private void writeStart(CodeWriter writer, HandleManager handleManager, String methodCallString) {
        if (usesClassDescHandle) {
            writer.writeUnsupportedHandleUsageComment("Class descriptor");
        }

        StringBuilder lineBuilder = new StringBuilder(methodCallString);
        if (ownHandle.isUsed()) {
            lineBuilder.append(handleManager.getHandleName(ownHandle));
            lineBuilder.append(", ");
        }

        Iterator<String> interfaceNamesIterator = interfaceNames.iterator();
        while (interfaceNamesIterator.hasNext()) {
            lineBuilder.append(LiteralsHelper.createStringLiteral(interfaceNamesIterator.next()));

            if (interfaceNamesIterator.hasNext()) {
                lineBuilder.append(", ");
            }
        }

        lineBuilder.append(')');
        writer.writeLine(lineBuilder.toString());
    }

    private void writeInvocationHandler(CodeWriter writer, HandleManager handleManager, VariableNameManager variableNameManager) {
        if (invocationHandler instanceof HandleObject handleObject) {
            writer.writeLine(".invocationHandlerHandle(" + handleManager.getHandleName(handleObject.handle()) + ')');
        } else if (invocationHandler instanceof SerializableObject serializableObject) {
            serializableObject.writeData(writer, handleManager, variableNameManager, ".beginSerializableInvocationHandler(", false);
        } else if (invocationHandler instanceof ExternalizableObject externalizableObject) {
            externalizableObject.writeData(writer, handleManager, variableNameManager, ".externalizableInvocationHandler(", false);
        } else if (invocationHandler instanceof ProxyObject proxyObject) {
            proxyObject.writeData(writer, handleManager, variableNameManager, ".beginProxyInvocationHandler(", false);
        } else {
            // Creator of this object should have already checked the type
            throw new AssertionError("Unsupported invocation handler " + invocationHandler);
        }
    }
}
