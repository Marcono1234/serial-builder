package marcono1234.serialization.serialbuilder.simplebuilder.api.object;

import marcono1234.serialization.serialbuilder.builder.api.Enclosing;
import marcono1234.serialization.serialbuilder.builder.api.Handle;
import marcono1234.serialization.serialbuilder.builder.api.ThrowingConsumer;
import marcono1234.serialization.serialbuilder.builder.implementation.ClassTypeNameHelper;
import marcono1234.serialization.serialbuilder.builder.implementation.SerialVersionUidHelper;
import marcono1234.serialization.serialbuilder.simplebuilder.api.ObjectBuildingDataOutput;
import marcono1234.serialization.serialbuilder.simplebuilder.api.object.array.ObjectArrayElements;
import marcono1234.serialization.serialbuilder.simplebuilder.api.object.proxy.ProxyObjectEnd;
import marcono1234.serialization.serialbuilder.simplebuilder.api.object.proxy.ProxyObjectStart;
import marcono1234.serialization.serialbuilder.simplebuilder.api.object.serializable.SerializableObjectStart;

import java.io.Externalizable;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.util.function.Function;

public interface ObjectStart<C> {
    /**
     * Writes a reference to an already written object using an assigned handle.
     *
     * @param handle
     *      an assigned handle referencing a previously written object
     * @return <i>next step</i>
     */
    C objectHandle(Handle handle);

    /**
     * Writes a {@code null} value.
     *
     * @return <i>next step</i>
     */
    C nullObject();

    /**
     * Writes a string.
     *
     * @return <i>next step</i>
     */
    C string(String s);

    // Note: Don't use varargs for primitive arrays to avoid calling wrong overload,
    // E.g. `array(Byte.MAX_VALUE, 1)` calling `array(int...)`

    /**
     * Writes an array and assigns a handle to it.
     *
     * @param unassignedHandle
     *      handle which should be assigned a reference to the written array
     * @param array
     *      array to write
     * @return <i>next step</i>
     */
    C array(Handle unassignedHandle, boolean[] array);
    /**
     * Writes an array.
     *
     * @param array
     *      array to write
     * @return <i>next step</i>
     */
    default C array(boolean[] array) {
        return array(new Handle(), array);
    }

    /**
     * Writes a {@code byte} array and assigns a handle to it.
     *
     * @param unassignedHandle
     *      handle which should be assigned a reference to the written array
     * @param array
     *      array to write
     * @return <i>next step</i>
     */
    C array(Handle unassignedHandle, byte[] array);
    /**
     * Writes a {@code byte} array.
     *
     * @param array
     *      array to write
     * @return <i>next step</i>
     */
    default C array(byte[] array) {
        return array(new Handle(), array);
    }

    /**
     * Writes a {@code char} array and assigns a handle to it.
     *
     * @param unassignedHandle
     *      handle which should be assigned a reference to the written array
     * @param array
     *      array to write
     * @return <i>next step</i>
     */
    C array(Handle unassignedHandle, char[] array);
    /**
     * Writes a {@code char} array.
     *
     * @param array
     *      array to write
     * @return <i>next step</i>
     */
    default C array(char[] array) {
        return array(new Handle(), array);
    }

    /**
     * Writes a {@code short} array and assigns a handle to it.
     *
     * @param unassignedHandle
     *      handle which should be assigned a reference to the written array
     * @param array
     *      array to write
     * @return <i>next step</i>
     */
    C array(Handle unassignedHandle, short[] array);
    /**
     * Writes a {@code short} array.
     *
     * @param array
     *      array to write
     * @return <i>next step</i>
     */
    default C array(short[] array) {
        return array(new Handle(), array);
    }

    /**
     * Writes an {@code int} array and assigns a handle to it.
     *
     * @param unassignedHandle
     *      handle which should be assigned a reference to the written array
     * @param array
     *      array to write
     * @return <i>next step</i>
     */
    C array(Handle unassignedHandle, int[] array);
    /**
     * Writes an {@code int} array.
     *
     * @param array
     *      array to write
     * @return <i>next step</i>
     */
    default C array(int[] array) {
        return array(new Handle(), array);
    }

    /**
     * Writes a {@code long} array and assigns a handle to it.
     *
     * @param unassignedHandle
     *      handle which should be assigned a reference to the written array
     * @param array
     *      array to write
     * @return <i>next step</i>
     */
    C array(Handle unassignedHandle, long[] array);
    /**
     * Writes a {@code long} array.
     *
     * @param array
     *      array to write
     * @return <i>next step</i>
     */
    default C array(long[] array) {
        return array(new Handle(), array);
    }

    /**
     * Writes a {@code float} array and assigns a handle to it.
     *
     * @param unassignedHandle
     *      handle which should be assigned a reference to the written array
     * @param array
     *      array to write
     * @return <i>next step</i>
     */
    C array(Handle unassignedHandle, float[] array);
    /**
     * Writes a {@code float} array.
     *
     * @param array
     *      array to write
     * @return <i>next step</i>
     */
    default C array(float[] array) {
        return array(new Handle(), array);
    }

    /**
     * Writes a {@code double} array and assigns a handle to it.
     *
     * @param unassignedHandle
     *      handle which should be assigned a reference to the written array
     * @param array
     *      array to write
     * @return <i>next step</i>
     */
    C array(Handle unassignedHandle, double[] array);
    /**
     * Writes a {@code double} array.
     *
     * @param array
     *      array to write
     * @return <i>next step</i>
     */
    default C array(double[] array) {
        return array(new Handle(), array);
    }

    /**
     * Begins an array of objects and assigns a handle to it. The next step are the array elements.
     *
     * @param unassignedHandle
     *      handle which should be assigned a reference to the written array
     * @param arrayType
     *      type of the array in the form returned by {@link Class#getTypeName()}, e.g.
     *      {@code java.util.Map$Entry[]}
     * @return <i>next step</i>
     */
    // Uses array type as parameter (instead of just component type) to avoid confusion
    // for multi-dimensional arrays where component type is array as well
    ObjectArrayElements<C> beginObjectArray(Handle unassignedHandle, String arrayType);

    /**
     * Begins an array of objects. The next step are the array elements.
     *
     * @param arrayType
     *      type of the array in the form returned by {@link Class#getTypeName()}, e.g.
     *      {@code java.util.Map$Entry[]}
     * @return <i>next step</i>
     */
    default ObjectArrayElements<C> beginObjectArray(String arrayType) {
        return beginObjectArray(new Handle(), arrayType);
    }

    private static String getArrayTypeName(Class<?> arrayType) {
        if (!arrayType.isArray()) {
            throw new IllegalArgumentException("Not an array type: " + arrayType.getTypeName());
        }
        if (arrayType.getComponentType().isPrimitive()) {
            throw new IllegalArgumentException("Primitive array instead of object array: " + arrayType.getTypeName());
        }
        return arrayType.getTypeName();
    }

    /**
     * Begins an array of objects and assigns a handle to it. The next step are the array elements.
     *
     * @param unassignedHandle
     *      handle which should be assigned a reference to the written array
     * @param arrayType
     *      type of the array, e.g. {@code String[].class}
     * @return <i>next step</i>
     */
    default ObjectArrayElements<C> beginObjectArray(Handle unassignedHandle, Class<?> arrayType) {
        return beginObjectArray(unassignedHandle, getArrayTypeName(arrayType));
    }

    /**
     * Begins an array of objects. The next step are the array elements.
     *
     * @param arrayType
     *      type of the array, e.g. {@code String[].class}
     * @return <i>next step</i>
     */
    default ObjectArrayElements<C> beginObjectArray(Class<?> arrayType) {
        return beginObjectArray(new Handle(), arrayType);
    }

    /**
     * Writes an array of objects and assigns a handle to it. Allows using a separate method for creating the array
     * elements without having to interrupt the builder call chain. The writer function must call all builder methods
     * and return the result of the last builder method to make sure the data is written correctly.
     *
     * @param unassignedHandle
     *      handle which should be assigned a reference to the written array
     * @param arrayType
     *      type of the array in the form returned by {@link Class#getTypeName()}, e.g.
     *      {@code java.util.Map$Entry[]} or {@code int[]}
     * @param writer
     *      for writing the array elements
     * @return <i>next step</i>
     */
    C objectArray(Handle unassignedHandle, String arrayType, Function<ObjectArrayElements<Enclosing>, Enclosing> writer);

    /**
     * Writes an array of objects. Allows using a separate method for creating the array
     * elements without having to interrupt the builder call chain. The writer function must call all builder methods
     * and return the result of the last builder method to make sure the data is written correctly.
     *
     * @param arrayType
     *      type of the array in the form returned by {@link Class#getTypeName()}, e.g.
     *      {@code java.util.Map$Entry[]} or {@code int[]}
     * @param writer
     *      for writing the array elements
     * @return <i>next step</i>
     */
    default C objectArray(String arrayType, Function<ObjectArrayElements<Enclosing>, Enclosing> writer) {
        return objectArray(new Handle(), arrayType, writer);
    }

    /**
     * Writes an array of objects and assigns a handle to it. Allows using a separate method for creating the array
     * elements without having to interrupt the builder call chain. The writer function must call all builder methods
     * and return the result of the last builder method to make sure the data is written correctly.
     *
     * @param unassignedHandle
     *      handle which should be assigned a reference to the written array
     * @param arrayType
     *      type of the array, e.g. {@code String[].class}
     * @param writer
     *      for writing the array elements
     * @return <i>next step</i>
     */
    default C objectArray(Handle unassignedHandle, Class<?> arrayType, Function<ObjectArrayElements<Enclosing>, Enclosing> writer) {
        return objectArray(unassignedHandle, getArrayTypeName(arrayType), writer);
    }

    /**
     * Writes an array of objects. Allows using a separate method for creating the array
     * elements without having to interrupt the builder call chain. The writer function must call all builder methods
     * and return the result of the last builder method to make sure the data is written correctly.
     *
     * @param arrayType
     *      type of the array, e.g. {@code String[].class}
     * @param writer
     *      for writing the array elements
     * @return <i>next step</i>
     */
    default C objectArray(Class<?> arrayType, Function<ObjectArrayElements<Enclosing>, Enclosing> writer) {
        return objectArray(new Handle(), arrayType, writer);
    }

    /**
     * Writes an enum constant.
     *
     * @param enumClass
     *      name of the enum class in the form returned by {@link Class#getTypeName()}, e.g.
     *      {@code java.lang.Thread$State}
     * @param constantName
     *      name of the enum constant
     * @return <i>next step</i>
     */
    C enumConstant(String enumClass, String constantName);

    /**
     * Writes an enum constant.
     *
     * @param enumClass
     *      enum class declaring the constant
     * @param constantName
     *      name of the enum constant
     * @return <i>next step</i>
     */
    default C enumConstant(Class<? extends Enum<?>> enumClass, String constantName) {
        return enumConstant(enumClass.getTypeName(), constantName);
    }

    /**
     * Writes an enum constant.
     *
     * @return <i>next step</i>
     */
    default <E extends Enum<?>> C enumConstant(E enumConstant) {
        return enumConstant(enumConstant.getDeclaringClass(), enumConstant.name());
    }

    /**
     * Writes a {@code Class} which does not implement {@link Serializable}.
     *
     * @param className
     *      name of the class in the form returned by {@link Class#getTypeName()}, e.g.
     *      {@code java.util.Map$Entry}
     * @return <i>next step</i>
     * @see #class_(Class)
     */
    // Setting type name is not enough, must also set the matching flags, otherwise validation in java.io.ObjectStreamClass.initNonProxy fails
    C nonSerializableClass(String className);

    /**
     * Writes a {@code Class} which implements {@link Externalizable}.
     *
     * @param className
     *      name of the class in the form returned by {@link Class#getTypeName()}, e.g.
     *      {@code java.util.Map$Entry}
     * @param serialVersionUID
     *      {@code serialVersionUID} of the class
     * @return <i>next step</i>
     * @see #class_(Class)
     */
    C externalizableClass(String className, long serialVersionUID);

    /**
     * Writes a {@code Class} which implements {@link Serializable}.
     *
     * @param className
     *      name of the class in the form returned by {@link Class#getTypeName()}, e.g.
     *      {@code java.util.Map$Entry}
     * @param serialVersionUID
     *      {@code serialVersionUID} of the class
     * @return <i>next step</i>
     * @see #class_(Class)
     */
    C serializableClass(String className, long serialVersionUID);

    /**
     * Writes a {@code Class} representing an array type.
     *
     * @param className
     *      name of the class in the form returned by {@link Class#getTypeName()}, e.g.
     *      {@code java.util.Map$Entry[]}
     * @return <i>next step</i>
     * @see #class_(Class)
     */
    // For array and record serialVersionUID is ignored, see https://docs.oracle.com/en/java/javase/17/docs/specs/serialization/class.html#stream-unique-identifiers
    default C arrayClass(String className) {
        return serializableClass(className, 0);
    }

    /**
     * Writes a {@code Class} representing a Java Record class.
     *
     * @param className
     *      name of the class in the form returned by {@link Class#getTypeName()}, e.g.
     *      {@code java.util.Map$Entry}
     * @return <i>next step</i>
     * @see #class_(Class)
     */
    default C recordClass(String className) {
        return serializableClass(className, 0);
    }

    /**
     * Writes a {@code Class} representing an enum class.
     *
     * @param className
     *      name of the class in the form returned by {@link Class#getTypeName()}, e.g.
     *      {@code java.util.Map$Entry}
     * @return <i>next step</i>
     * @see #class_(Class)
     * @see #enumConstant(String, String)
     */
    C enumClass(String className);

    /**
     * Writes a {@code Class} representing a {@link Proxy} class.
     *
     * @param interfaceNames
     *      names of the interfaces implemented by the proxy, in the form returned by {@link Class#getTypeName()}, e.g.
     *      {@code java.util.Map$Entry}
     * @return <i>next step</i>
     * @see #class_(Class)
     */
    C proxyClass(String... interfaceNames);

    /**
     * Writes a {@code Class} representing a {@link Proxy} class.
     *
     * @param interfaces
     *      interfaces implemented by the proxy
     * @return <i>next step</i>
     * @see #class_(Class)
     */
    default C proxyClass(Class<?>... interfaces) {
        return proxyClass(ClassTypeNameHelper.getInterfaceNames(interfaces));
    }

    /**
     * Writes a {@code Class}. The type of the class (e.g. whether it implements {@link Serializable}) as well as the
     * {@code serialVersionUID} (if needed) are determined automatically.
     *
     * @return <i>next step</i>
     */
    default C class_(Class<?> c) {
        String className = c.getTypeName();

        if (Proxy.isProxyClass(c)) {
            return proxyClass(c.getInterfaces());
        }
        // ObjectStreamClass also considers Enum.class to be an enum (unlike `Class.isEnum()`)
        else if (c.isEnum() || c == Enum.class) {
            return enumClass(className);
        } else if (c.isArray()) {
            return arrayClass(className);
        } else if (c.isRecord()) {
            return recordClass(className);
        } else if (Externalizable.class.isAssignableFrom(c)) {
            return externalizableClass(className, SerialVersionUidHelper.getSerialVersionUID(c));
        } else if (Serializable.class.isAssignableFrom(c)){
            return serializableClass(className, SerialVersionUidHelper.getSerialVersionUID(c));
        } else {
            return nonSerializableClass(className);
        }
    }

    /**
     * Begins an object implementing {@link Serializable} and assigns a handle to it. The next step is the list of
     * class data for the object, starting at the supertypes (if any) and ending with the data for the class itself.
     *
     * @param unassignedHandle
     *      handle which should be assigned a reference to the written object
     * @return <i>next step</i>
     */
    SerializableObjectStart<C> beginSerializableObject(Handle unassignedHandle);

    /**
     * Begins an object implementing {@link Serializable}. The next step is the list of
     * class data for the object, starting at the supertypes (if any) and ending with the data for the class itself.
     *
     * @return <i>next step</i>
     */
    default SerializableObjectStart<C> beginSerializableObject() {
        return beginSerializableObject(new Handle());
    }

    /**
     * Writes a new object implementing {@link Serializable} and assigns a handle to it. Allows using a separate method
     * for creating the object data without having to interrupt the builder call chain. The writer function must call
     * all builder methods and return the result of the last builder method to make sure the data is written correctly.
     *
     * @param unassignedHandle
     *      handle which should be assigned a reference to the written object
     * @param writer
     *      for writing the object data
     * @return <i>next step</i>
     */
    /*
     * Note: Unlike the other methods with `Function<..., Enclosing>`, SerializableObjectStart is only a list of class
     * data, therefore `endObject()` call at the end returning Enclosing is required, even though it makes the call
     * chain unbalanced, but that is probably acceptable. E.g.:
     * serializableObject(writer -> {
     *     return writer
     *         .beginClassData(...)
     *         .endClassData()
     *         .beginClassData(...)
     *         .endClassData()
     *     .endObject();
     * });
     */
    C serializableObject(Handle unassignedHandle, Function<SerializableObjectStart<Enclosing>, Enclosing> writer);

    /**
     * Writes a new object implementing {@link Serializable}. Allows using a separate method
     * for creating the object data without having to interrupt the builder call chain. The writer function must call
     * all builder methods and return the result of the last builder method to make sure the data is written correctly.
     *
     * @param writer
     *      for writing the object data
     * @return <i>next step</i>
     */
    default C serializableObject(Function<SerializableObjectStart<Enclosing>, Enclosing> writer) {
        return serializableObject(new Handle(), writer);
    }

    /**
     * Writes an object implementing {@link Externalizable} and assigns a handle to it.
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
     * @return <i>next step</i>
     */
    C externalizableObject(Handle unassignedHandle, String typeName, long serialVersionUID, ThrowingConsumer<ObjectBuildingDataOutput> writer);

    /**
     * Writes an object implementing {@link Externalizable}.
     *
     * @param typeName
     *      name of the {@code Externalizable} type, in the form returned by {@link Class#getTypeName()},
     *      e.g. {@code java.util.Map$Entry}
     * @param serialVersionUID
     *      value of the {@code serialVersionUID} field (or the calculated UID value)
     * @param writer
     *      for writing the data written by {@link java.io.Externalizable#writeExternal(ObjectOutput)}
     * @return <i>next step</i>
     */
    default C externalizableObject(String typeName, long serialVersionUID, ThrowingConsumer<ObjectBuildingDataOutput> writer) {
        return externalizableObject(new Handle(), typeName, serialVersionUID, writer);
    }

    /**
     * Writes an object implementing {@link Externalizable} and assigns a handle to it.
     *
     * @param unassignedHandle
     *      handle which should be assigned a reference to the written object
     * @param c
     *      the {@code Externalizable} type
     * @param writer
     *      for writing the data written by {@link java.io.Externalizable#writeExternal(ObjectOutput)}
     * @return <i>next step</i>
     */
    default C externalizableObject(Handle unassignedHandle, Class<? extends Externalizable> c, ThrowingConsumer<ObjectBuildingDataOutput> writer) {
        return externalizableObject(unassignedHandle, c.getTypeName(), SerialVersionUidHelper.getSerialVersionUID(c), writer);
    }

    /**
     * Writes an object implementing {@link Externalizable}.
     *
     * @param c
     *      the {@code Externalizable} type
     * @param writer
     *      for writing the data written by {@link java.io.Externalizable#writeExternal(ObjectOutput)}
     * @return <i>next step</i>
     */
    default C externalizableObject(Class<? extends Externalizable> c, ThrowingConsumer<ObjectBuildingDataOutput> writer) {
        return externalizableObject(new Handle(), c, writer);
    }

    /**
     * Begins a new {@link Proxy} object and assigns a handle to it.
     *
     * @param unassignedHandle
     *      handle which should be assigned a reference to the written object
     * @param interfaceNames
     *      names of the interface types implemented by the proxy, in the form returned by {@link Class#getTypeName()},
     *      e.g. {@code java.util.Map$Entry}
     * @return <i>next step</i>
     */
    // Include ProxyObjectEnd here in type argument instead of specifying it in return type of ProxyObjectStart methods
    // to allow `proxyObject` methods below to avoid redundant ending call in writer implementation
    ProxyObjectStart<ProxyObjectEnd<C>> beginProxyObject(Handle unassignedHandle, String... interfaceNames);

    /**
     * Begins a new {@link Proxy} object.
     *
     * @param interfaceNames
     *      names of the interface types implemented by the proxy, in the form returned by {@link Class#getTypeName()},
     *      e.g. {@code java.util.Map$Entry}
     * @return <i>next step</i>
     */
    default ProxyObjectStart<ProxyObjectEnd<C>> beginProxyObject(String... interfaceNames) {
        return beginProxyObject(new Handle(), interfaceNames);
    }

    /**
     * Begins a new {@link Proxy} object and assigns a handle to it.
     *
     * @param unassignedHandle
     *      handle which should be assigned a reference to the written object
     * @param interfaces
     *      interface types implemented by the proxy
     * @return <i>next step</i>
     */
    default ProxyObjectStart<ProxyObjectEnd<C>> beginProxyObject(Handle unassignedHandle, Class<?>... interfaces) {
        return beginProxyObject(unassignedHandle, ClassTypeNameHelper.getInterfaceNames(interfaces));
    }

    /**
     * Begins a new {@link Proxy} object.
     *
     * @param interfaces
     *      interface types implemented by the proxy
     * @return <i>next step</i>
     */
    default ProxyObjectStart<ProxyObjectEnd<C>> beginProxyObject(Class<?>... interfaces) {
        return beginProxyObject(new Handle(), interfaces);
    }

    /**
     * Writes a new {@link Proxy} object and assigns a handle to it. Allows using a separate method for creating the
     * invocation handler without having to interrupt the builder call chain. The writer function must call all builder
     * methods and return the result of the last builder method to make sure the data is written correctly.
     *
     * @param unassignedHandle
     *      handle which should be assigned a reference to the written object
     * @param interfaceNames
     *      names of the interface types implemented by the proxy, in the form returned by {@link Class#getTypeName()},
     *      e.g. {@code java.util.Map$Entry}
     * @param writer
     *      for writing the invocation handler of the proxy
     * @return <i>next step</i>
     */
    C proxyObject(Handle unassignedHandle, String[] interfaceNames, Function<ProxyObjectStart<Enclosing>, Enclosing> writer);

    /**
     * Writes a new {@link Proxy} object. Allows using a separate method for creating the
     * invocation handler without having to interrupt the builder call chain. The writer function must call all builder
     * methods and return the result of the last builder method to make sure the data is written correctly.
     *
     * @param interfaceNames
     *      names of the interface types implemented by the proxy, in the form returned by {@link Class#getTypeName()},
     *      e.g. {@code java.util.Map$Entry}
     * @param writer
     *      for writing the invocation handler of the proxy
     * @return <i>next step</i>
     */
    default C proxyObject(String[] interfaceNames, Function<ProxyObjectStart<Enclosing>, Enclosing> writer) {
        return proxyObject(new Handle(), interfaceNames, writer);
    }

    /**
     * Writes a new {@link Proxy} object and assigns a handle to it. Allows using a separate method for creating the
     * invocation handler without having to interrupt the builder call chain. The writer function must call all builder
     * methods and return the result of the last builder method to make sure the data is written correctly.
     *
     * @param unassignedHandle
     *      handle which should be assigned a reference to the written object
     * @param interfaces
     *      interfaces implemented by the proxy
     * @param writer
     *      for writing the invocation handler of the proxy
     * @return <i>next step</i>
     */
    default C proxyObject(Handle unassignedHandle, Class<?>[] interfaces, Function<ProxyObjectStart<Enclosing>, Enclosing> writer) {
        return proxyObject(unassignedHandle, ClassTypeNameHelper.getInterfaceNames(interfaces), writer);
    }

    /**
     * Writes a new {@link Proxy} object. Allows using a separate method for creating the
     * invocation handler without having to interrupt the builder call chain. The writer function must call all builder
     * methods and return the result of the last builder method to make sure the data is written correctly.
     *
     * @param interfaces
     *      interfaces implemented by the proxy
     * @param writer
     *      for writing the invocation handler of the proxy
     * @return <i>next step</i>
     */
    default C proxyObject(Class<?>[] interfaces, Function<ProxyObjectStart<Enclosing>, Enclosing> writer) {
        return proxyObject(new Handle(), interfaces, writer);
    }
}
