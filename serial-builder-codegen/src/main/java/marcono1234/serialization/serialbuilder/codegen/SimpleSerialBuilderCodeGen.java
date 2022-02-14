package marcono1234.serialization.serialbuilder.codegen;

import marcono1234.serialization.serialbuilder.codegen.implementation.SerialDataCodeGen;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Java code generator which generates for the given serialization data the corresponding {@code marcono1234.serialization.serialbuilder.SimpleSerialBuilder}
 * calls recreating that serialization data (as close as possible).
 */
public class SimpleSerialBuilderCodeGen {
    private SimpleSerialBuilderCodeGen() {
    }

    /**
     * For the given serialization data generates the corresponding {@code marcono1234.serialization.serialbuilder.SimpleSerialBuilder}
     * calls recreating that serialization data (as close as possible). The generated code only represents the snippet
     * invoking the {@code SimpleSerialBuilder} methods, it cannot be compiled on its own. The following imports are
     * necessary to use the generated code:
     * <pre>{@code
     * import marcono1234.serialization.serialbuilder.SimpleSerialBuilder;
     * import marcono1234.serialization.serialbuilder.builder.api.Handle;
     * }</pre>
     *
     * <p>The generated code tries to recreate the serialization data as close as possible. In cases where the
     * {@code SimpleSerialBuilder} API does not allow to recreate the data exactly, but where that difference should
     * normally not have any effect, an inline comment is written in the code. For cases where the serialization
     * data cannot be recreated at all, a {@link CodeGenException} is thrown. That should normally only happen when
     * the data is valid according to the serialization format grammar, but cannot actually be read by
     * {@link java.io.ObjectInputStream}, or when it is not 'interesting' and is therefore not supported by the
     * serial-builder API.
     *
     * <p><b>Warning:</b> This method should not be called with untrusted serialization data. Malicious serialization
     * data might be able to cause a denial of service for this method, or might be able to manipulate the generated
     * code to perform malicious actions.
     *
     * <h4>Example</h4>
     * Let's assume you want to get the necessary {@code SimpleSerialBuilder} calls to create an instance of
     * {@code java.util.ArrayList}. Then you can call this method like this:
     * <pre>{@code
     * // In this example the existing serialization data is created here, but it could also come from a different source
     * ByteArrayOutputStream out = new ByteArrayOutputStream();
     * try (ObjectOutputStream objOut = new ObjectOutputStream(out)) {
     *     ArrayList<Object> list = new ArrayList<>();
     *     list.add("test");
     *     list.add(list); // add itself
     *     objOut.writeObject(list);
     * }
     *
     * byte[] existingSerialData = out.toByteArray();
     * String generatedCode = SimpleSerialBuilderCodeGen.generateCode(existingSerialData);
     * }</pre>
     *
     * <p>The generated code would be:
     * <pre>{@code
     * Handle handle1 = new Handle();
     *
     * byte[] serialData = SimpleSerialBuilder.startSerializableObject(handle1)
     *     .beginClassData("java.util.ArrayList", 8683452581122892189L)
     *         .primitiveIntField("size", 2)
     *         .writeObjectWith(writer -> {
     *             writer.write(new byte[] {0, 0, 0, 2});
     *             writer.string("test");
     *             writer.objectHandle(handle1);
     *         })
     *     .endClassData()
     * .endObject();
     * }</pre>
     * (depending on the JDK version used to create the serialization data, the output might differ)
     *
     * @param serializationData
     *      The existing serialization data; this is all the data written to the {@code OutputStream} provided to the
     *      constructor of {@link java.io.ObjectOutputStream}
     * @return
     *      The generated Java code
     * @throws CodeGenException
     *      If code generation fails; either because the serialization data is malformed, or because it cannot be
     *      recreated using the {@code SimpleSerialBuilder}
     */
    public static String generateCode(byte[] serializationData) throws CodeGenException {
        try (SerialDataCodeGen codeGen = new SerialDataCodeGen(new ByteArrayInputStream(serializationData))) {
            return codeGen.generateCode(true, true);
        } catch (IOException e) {
            throw new CodeGenException("Code generation failed: " + e.getMessage(), e);
        }
    }
}
