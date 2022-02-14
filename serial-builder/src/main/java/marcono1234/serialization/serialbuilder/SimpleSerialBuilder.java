package marcono1234.serialization.serialbuilder;

import marcono1234.serialization.serialbuilder.builder.api.Handle;
import marcono1234.serialization.serialbuilder.builder.api.ThrowingConsumer;
import marcono1234.serialization.serialbuilder.builder.implementation.ClassTypeNameHelper;
import marcono1234.serialization.serialbuilder.builder.implementation.SerialVersionUidHelper;
import marcono1234.serialization.serialbuilder.simplebuilder.api.ObjectBuildingDataOutput;
import marcono1234.serialization.serialbuilder.simplebuilder.api.object.proxy.ProxyObjectEnd;
import marcono1234.serialization.serialbuilder.simplebuilder.api.object.proxy.ProxyObjectStart;
import marcono1234.serialization.serialbuilder.simplebuilder.api.object.serializable.SerializableObjectStart;
import marcono1234.serialization.serialbuilder.simplebuilder.implementation.SimpleSerialBuilderImpl;

import java.io.Externalizable;
import java.io.ObjectOutput;
import java.util.Objects;

/**
 * Provides static entry points for creating Java serialization data. This API is designed for creating serialization
 * data at a higher level than {@link SerialBuilder}. It is more concise and through its structure prevents most cases
 * of invalid or malformed data. The downside of this is that, unlike {@code SerialBuilder}, it might not allow creating
 * serialization data for certain corner cases, but for the majority of use cases it should suffice.
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
 * class SerializableClass implements Serializable {
 *     @Serial
 *     private static final long serialVersionUID = 1L;
 *
 *     public int i;
 *     public int[] array;
 *     public String s;
 * }
 * }</pre>
 *
 * Then the API can be used in the following way to produce serialization data:
 * <pre>{@code
 * byte[] serialData = SimpleSerialBuilder.startSerializableObject()
 *     .beginClassData(SerializableClass.class)
 *         .primitiveIntField("i", 6)
 *         .beginObjectField("array", int[].class)
 *             .array(new int[] {1, 2, 3})
 *         .endField()
 *         .beginObjectField("s", String.class)
 *             .string("nested-test")
 *         .endField()
 *     .endClassData()
 * .endObject();
 * }</pre>
 *
 * @see SerialBuilder
 */
public class SimpleSerialBuilder {
    private SimpleSerialBuilder() { }

    /**
     * Start of an object implementing {@link java.io.Serializable}.
     */
    public interface SerializableBuilderStart extends SerializableObjectStart<byte[]> {
    }

    /**
     * Start of a {@link java.lang.reflect.Proxy} object.
     */
    public interface ProxyBuilderStart extends ProxyObjectStart<ProxyObjectEnd<byte[]>> {
    }

    /**
     * Starts a new object implementing {@link java.io.Serializable} and assigns a handle to it. The next step is the
     * list of class data for the object, starting at the supertypes (if any) and ending with the data for the class itself.
     *
     * @param unassignedHandle
     *      handle which should be assigned a reference to the written object
     * @return <i>next step</i>
     */
    public static SerializableBuilderStart startSerializableObject(Handle unassignedHandle) {
        return SimpleSerialBuilderImpl.startSerializable(unassignedHandle);
    }

    /**
     * Starts a new object implementing {@link java.io.Serializable}. The next step is the list of class data for the
     * object, starting at the supertypes (if any) and ending with the data for the class itself.
     *
     * @return <i>next step</i>
     */
    public static SerializableBuilderStart startSerializableObject() {
        return startSerializableObject(new Handle());
    }

    /**
     * Creates a new object implementing {@link java.io.Externalizable} and assigns a handle to it.
     *
     * @param unassignedHandle
     *      handle which should be assigned a reference to the written object
     * @param typeName
     *      name of the {@code Externalizable} type, in the form returned by {@link Class#getTypeName()},
     *      e.g. {@code java.util.Map$Entry}
     * @param serialVersionUID
     *      value of the {@code serialVersionUID} field (or the calculated UID value)
     * @param writer
     *      for writing the data written by {@link java.io.Externalizable#writeExternal(ObjectOutput)}
     * @return the serialized data
     */
    public static byte[] externalizableObject(Handle unassignedHandle, String typeName, long serialVersionUID, ThrowingConsumer<ObjectBuildingDataOutput> writer) {
        return SimpleSerialBuilderImpl.createExternalizable(unassignedHandle, typeName, serialVersionUID, writer);
    }

    /**
     * Creates a new object implementing {@link java.io.Externalizable}.
     *
     * @param typeName
     *      name of the {@code Externalizable} type, in the form returned by {@link Class#getTypeName()},
     *      e.g. {@code java.util.Map$Entry}
     * @param serialVersionUID
     *      value of the {@code serialVersionUID} field (or the calculated UID value)
     * @param writer
     *      for writing the data written by {@link java.io.Externalizable#writeExternal(ObjectOutput)}
     * @return the serialized data
     */
    public static byte[] externalizableObject(String typeName, long serialVersionUID, ThrowingConsumer<ObjectBuildingDataOutput> writer) {
        return externalizableObject(new Handle(), typeName, serialVersionUID, writer);
    }

    /**
     * Creates a new object implementing {@link java.io.Externalizable} and assigns a handle to it.
     *
     * @param unassignedHandle
     *      handle which should be assigned a reference to the written object
     * @param c
     *      the {@code Externalizable} type
     * @param writer
     *      for writing the data written by {@link java.io.Externalizable#writeExternal(ObjectOutput)}
     * @return the serialized data
     */
    public static byte[] externalizableObject(Handle unassignedHandle, Class<? extends Externalizable> c, ThrowingConsumer<ObjectBuildingDataOutput> writer) {
        return externalizableObject(unassignedHandle, c.getTypeName(), SerialVersionUidHelper.getSerialVersionUID(c), writer);
    }

    /**
     * Creates a new object implementing {@link java.io.Externalizable}.
     *
     * @param c
     *      the {@code Externalizable} type
     * @param writer
     *      for writing the data written by {@link java.io.Externalizable#writeExternal(ObjectOutput)}
     * @return the serialized data
     */
    public static byte[] externalizableObject(Class<? extends Externalizable> c, ThrowingConsumer<ObjectBuildingDataOutput> writer) {
        return externalizableObject(new Handle(), c, writer);
    }

    /**
     * Starts a new {@link java.lang.reflect.Proxy} object and assigns a handle to it.
     *
     * @param unassignedHandle
     *      handle which should be assigned a reference to the written object
     * @param interfaceNames
     *      names of the interface types implemented by the proxy, in the form returned by {@link Class#getTypeName()},
     *      e.g. {@code java.util.Map$Entry}
     * @return <i>next step</i>
     */
    public static ProxyBuilderStart startProxyObject(Handle unassignedHandle, String... interfaceNames) {
        return SimpleSerialBuilderImpl.startProxy(unassignedHandle, interfaceNames);
    }

    /**
     * Starts a new {@link java.lang.reflect.Proxy} object.
     *
     * @param interfaceNames
     *      names of the interface types implemented by the proxy, in the form returned by {@link Class#getTypeName()},
     *      e.g. {@code java.util.Map$Entry}
     * @return <i>next step</i>
     */
    public static ProxyBuilderStart startProxyObject(String... interfaceNames) {
        return startProxyObject(new Handle(), interfaceNames);
    }

    /**
     * Starts a new {@link java.lang.reflect.Proxy} object and assigns a handle to it.
     *
     * @param unassignedHandle
     *      handle which should be assigned a reference to the written object
     * @param interfaces
     *      interface types implemented by the proxy
     * @return <i>next step</i>
     */
    public static ProxyBuilderStart startProxyObject(Handle unassignedHandle, Class<?>... interfaces) {
        return startProxyObject(unassignedHandle, ClassTypeNameHelper.getInterfaceNames(interfaces));
    }

    /**
     * Starts a new {@link java.lang.reflect.Proxy} object.
     *
     * @param interfaces
     *      interface types implemented by the proxy
     * @return <i>next step</i>
     */
    public static ProxyBuilderStart startProxyObject(Class<?>... interfaces) {
        return startProxyObject(new Handle(), interfaces);
    }

    /**
     * Writes serialization data using an {@link ObjectBuildingDataOutput}. This allows writing top level
     * block data, writing multiple top level objects and writing top level objects for which no dedicated
     * builder method is provided by this class.
     *
     * <p>This method is mainly intended for the special cases listed above. If only a single object should
     * be written, the other builder methods of this class (such as {@link #startSerializableObject()}) should
     * be preferred because their usage is more concise.
     *
     * @param writer
     *      writes the objects and block data content
     * @return the serialization data
     */
    public static byte[] writeSerializationDataWith(ThrowingConsumer<ObjectBuildingDataOutput> writer) {
        Objects.requireNonNull(writer);
        return SimpleSerialBuilderImpl.writeSerializationDataWith(writer);
    }
}
