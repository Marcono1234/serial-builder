package marcono1234.serialization.serialbuilder.builder.api.object;

import marcono1234.serialization.serialbuilder.builder.api.Enclosing;
import marcono1234.serialization.serialbuilder.builder.api.Handle;
import marcono1234.serialization.serialbuilder.builder.api.descriptor.DescriptorHierarchyStart;
import marcono1234.serialization.serialbuilder.builder.api.object.array.ArrayElements;
import marcono1234.serialization.serialbuilder.builder.api.object.externalizable.ExternalizableObjectStart;
import marcono1234.serialization.serialbuilder.builder.api.object.serializable.SerializableObjectStart;

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
    default C string(String s) {
        return string(new Handle(), s);
    }

    /**
     * Writes a string and assigns a handle to it.
     *
     * @param unassignedHandle
     *      handle which should be assigned a reference to the written string
     * @return <i>next step</i>
     */
    C string(Handle unassignedHandle, String s);

    /**
     * Begins an array. The next step is the descriptor hierarchy representing the type of the array.
     *
     * @return <i>next step</i>
     */
    default DescriptorHierarchyStart<ArrayElements<C>> beginArray() {
        return beginArray(new Handle());
    }

    /**
     * Begins an array and assigns a handle to it. The next step is the descriptor hierarchy representing the
     * type of the array.
     *
     * @param unassignedHandle
     *      handle which should be assigned a reference to the written array
     * @return <i>next step</i>
     */
    DescriptorHierarchyStart<ArrayElements<C>> beginArray(Handle unassignedHandle);

    /**
     * Writes an array. Allows using a separate method for creating the array without having to interrupt the
     * builder call chain. The writer function must call all builder methods and return the result of the last
     * builder method to make sure the data is written correctly.
     *
     * @param writer
     *      for writing the array
     * @return <i>next step</i>
     */
    // Note: Function instead of Consumer is used as parameter type to force user to make all necessary calls
    // and return result of last call
    default C array(Function<DescriptorHierarchyStart<ArrayElements<Enclosing>>, Enclosing> writer) {
        return array(new Handle(), writer);
    }

    /**
     * Writes an array and assigns a handle to it. Allows using a separate method for creating the array without
     * having to interrupt the builder call chain. The writer function must call all builder methods and return
     * the result of the last builder method to make sure the data is written correctly.
     *
     * @param writer
     *      for writing the array
     * @param unassignedHandle
     *      handle which should be assigned a reference to the written array
     * @return <i>next step</i>
     */
    C array(Handle unassignedHandle, Function<DescriptorHierarchyStart<ArrayElements<Enclosing>>, Enclosing> writer);

    /**
     * Begins an enum constant. The next step is the descriptor hierarchy representing the type of the enum.
     *
     * @return <i>next step</i>
     */
    default DescriptorHierarchyStart<EnumStart<C>> beginEnum() {
        return beginEnum(new Handle());
    }

    /**
     * Begins an enum constant and assigns a handle to it. The next step is the descriptor hierarchy representing
     * the type of the enum.
     *
     * @param unassignedHandle
     *      handle which should be assigned a reference to the written enum constant
     * @return <i>next step</i>
     */
    DescriptorHierarchyStart<EnumStart<C>> beginEnum(Handle unassignedHandle);

    /**
     * Begins a {@code Class} object. The next step is the descriptor hierarchy representing the class.
     *
     * @return <i>next step</i>
     */
    default DescriptorHierarchyStart<ClassEnd<C>> beginClass() {
        return beginClass(new Handle());
    }

    /**
     * Begins a {@code Class} object and assigns a handle to it. The next step is the descriptor hierarchy
     * representing the class.
     *
     * @param unassignedHandle
     *      handle which should be assigned a reference to the written class
     * @return <i>next step</i>
     */
    DescriptorHierarchyStart<ClassEnd<C>> beginClass(Handle unassignedHandle);

    /**
     * Begins an object implementing {@link java.io.Serializable}. The next step is the descriptor hierarchy
     * representing the type of the object.
     *
     * @return <i>next step</i>
     */
    default DescriptorHierarchyStart<SerializableObjectStart<C>> beginSerializableObject() {
        return beginSerializableObject(new Handle());
    }

    /**
     * Begins an object implementing {@link java.io.Serializable} and assigns a handle to it. The next step is
     * the descriptor hierarchy representing the type of the object.
     *
     * @param unassignedHandle
     *      handle which should be assigned a reference to the written object
     * @return <i>next step</i>
     */
    DescriptorHierarchyStart<SerializableObjectStart<C>> beginSerializableObject(Handle unassignedHandle);

    /**
     * Writes an object implementing {@link java.io.Serializable}. Allows using a separate method for creating
     * the array without having to interrupt the builder call chain. The writer function must call all builder
     * methods and return the result of the last builder method to make sure the data is written correctly.
     *
     * @param writer
     *      for writing the object
     * @return <i>next step</i>
     */
    // Note: Function instead of Consumer is used as parameter type to force user to make all necessary calls
    // and return result of last call
    default C serializableObject(Function<DescriptorHierarchyStart<SerializableObjectStart<Enclosing>>, Enclosing> writer) {
        return serializableObject(new Handle(), writer);
    }

    /**
     * Writes an object implementing {@link java.io.Serializable} and assigns a handle to it. Allows using an
     * external method for creating the array without having to interrupt the builder call chain. The writer
     * function must call all builder methods and return the result of the last builder method to make sure the
     * data is written correctly.
     *
     * @param unassignedHandle
     *      handle which should be assigned a reference to the written object
     * @param writer
     *      for writing the object
     * @return <i>next step</i>
     */
    C serializableObject(Handle unassignedHandle, Function<DescriptorHierarchyStart<SerializableObjectStart<Enclosing>>, Enclosing> writer);

    /**
     * Begins an object implementing {@link java.io.Externalizable}. The next step is the descriptor hierarchy
     * representing the type of the object.
     *
     * @return <i>next step</i>
     */
    default DescriptorHierarchyStart<ExternalizableObjectStart<C>> beginExternalizableObject(){
        return beginExternalizableObject(new Handle());
    }

    /**
     * Begins an object implementing {@link java.io.Externalizable} and assigns a handle to it. The next step is
     * the descriptor hierarchy representing the type of the object.
     *
     * @param unassignedHandle
     *      handle which should be assigned a reference to the written object
     * @return <i>next step</i>
     */
    DescriptorHierarchyStart<ExternalizableObjectStart<C>> beginExternalizableObject(Handle unassignedHandle);

    /**
     * Writes an object implementing {@link java.io.Externalizable}. Allows using a separate method for creating
     * the array without having to interrupt the builder call chain. The writer function must call all builder
     * methods and return the result of the last builder method to make sure the data is written correctly.
     *
     * @param writer
     *      for writing the object
     * @return <i>next step</i>
     */
    // Note: Function instead of Consumer is used as parameter type to force user to make all necessary calls
    // and return result of last call
    default C externalizableObject(Function<DescriptorHierarchyStart<ExternalizableObjectStart<Enclosing>>, Enclosing> writer) {
        return externalizableObject(new Handle(), writer);
    }

    /**
     * Writes an object implementing {@link java.io.Externalizable} and assigns a handle to it. Allows using an
     * external method for creating the array without having to interrupt the builder call chain. The writer
     * function must call all builder methods and return the result of the last builder method to make sure the
     * data is written correctly.
     *
     * @param writer
     *      for writing the object
     * @param unassignedHandle
     *      handle which should be assigned a reference to the written object
     * @return <i>next step</i>
     */
    C externalizableObject(Handle unassignedHandle, Function<DescriptorHierarchyStart<ExternalizableObjectStart<Enclosing>>, Enclosing> writer);
}
