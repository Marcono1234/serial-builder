package marcono1234.serialization.serialbuilder;

import marcono1234.serialization.serialbuilder.builder.api.Handle;
import marcono1234.serialization.serialbuilder.builder.api.descriptor.DescriptorHierarchyStart;
import marcono1234.serialization.serialbuilder.builder.api.object.externalizable.ExternalizableObjectStart;
import marcono1234.serialization.serialbuilder.builder.api.object.serializable.SerializableObjectStart;
import marcono1234.serialization.serialbuilder.builder.implementation.SerialBuilderImpl;

/**
 * Provides static entry points for creating Java serialization data. The API structure follows closely the internal
 * Java serialization structure. This allows creating serialization data at a very low level, at the cost of verbose
 * API usage and reduced usage aid. In general this API performs little to no validation of arguments, which can lead
 * to serialization data which is either invalid / malformed, or which cannot be read by {@link java.io.ObjectInputStream}.
 * Prefer {@link SimpleSerialBuilder} if you want to create serialization data at a higher level, in a more concise and
 * safer way.
 *
 * <h2>Usage</h2>
 * This API is only intended to be used in a 'fluent builder style', where all methods calls are chained after each other
 * (with indentation to increase readability) until the end of the chain is reached, and the resulting serialization
 * data in the form of {@code byte[]} is returned. Using the API in any other way is not supported and might cause
 * exceptions. It is recommended to follow the IDE code completion suggestions while using the API, looking at the
 * builder API interfaces is most likely not that helpful.
 *
 * <h3>Example usage</h3>
 * Let's assume you have the following class implementing {@link java.io.Serializable}:
 * <pre>{@code
 * class SimpleSerializableClass implements Serializable {
 *     @Serial
 *     private static final long serialVersionUID = 1L;
 *
 *     public int i;
 *
 *     public SimpleSerializableClass(int i) {
 *         this.i = i;
 *     }
 * }
 * }</pre>
 *
 * Then the API can be used in the following way to produce serialization data:
 * <pre>{@code
 * byte[] actualData = SerialBuilder.startSerializableObject()
 *     .beginDescriptorHierarchy()
 *         .beginDescriptor()
 *             .type(SimpleSerializableClass.class)
 *             .uid(SimpleSerializableClass.serialVersionUID)
 *             .flags(SC_SERIALIZABLE)
 *             .beginPrimitiveFieldDescriptors()
 *                 .intField("i")
 *             .endPrimitiveFieldDescriptors()
 *         .endDescriptor()
 *     .endDescriptorHierarchy()
 *     .beginSlots()
 *         .beginSlot()
 *             .beginPrimitiveFields()
 *                 .value(1)
 *             .endPrimitiveFields()
 *         .endSlot()
 *     .endSlots()
 * .endObject();
 * }</pre>
 *
 * @see SimpleSerialBuilder
 */
public class SerialBuilder {
    private SerialBuilder() { }

    /**
     * Start of an object implementing {@link java.io.Serializable}.
     */
    public interface SerializableBuilderStart extends DescriptorHierarchyStart<SerializableObjectStart<byte[]>> {
    }

    /**
     * Start of an object implementing {@link java.io.Externalizable}.
     */
    public interface ExternalizableBuilderStart extends DescriptorHierarchyStart<ExternalizableObjectStart<byte[]>> {
    }

    /**
     * Starts a new object implementing {@link java.io.Serializable} and assigns a handle to it. The next step returned
     * by this method is the descriptor hierarchy representing the type of the object.
     *
     * @param unassignedHandle
     *      handle which should be assigned a reference to the written object
     * @return <i>next step</i>
     */
    public static SerializableBuilderStart startSerializableObject(Handle unassignedHandle) {
        return SerialBuilderImpl.startSerializable(unassignedHandle);
    }

    /**
     * Starts a new object implementing {@link java.io.Serializable}. The next step returned by this method
     * is the descriptor hierarchy representing the type of the object.
     *
     * @return <i>next step</i>
     */
    public static SerializableBuilderStart startSerializableObject() {
        return startSerializableObject(new Handle());
    }

    /**
     * Starts a new object implementing {@link java.io.Externalizable} and assigns a handle to it. The next step
     * returned by this method is the descriptor hierarchy representing the type of the object.
     *
     * @param unassignedHandle
     *      handle which should be assigned a reference to the written object
     * @return <i>next step</i>
     */
    public static ExternalizableBuilderStart startExternalizableObject(Handle unassignedHandle) {
        return SerialBuilderImpl.startExternalizable(unassignedHandle);
    }

    /**
     * Starts a new object implementing {@link java.io.Externalizable}. The next step returned by this method
     * is the descriptor hierarchy representing the type of the object.
     *
     * @return <i>next step</i>
     */
    public static ExternalizableBuilderStart startExternalizableObject() {
        return startExternalizableObject(new Handle());
    }
}
