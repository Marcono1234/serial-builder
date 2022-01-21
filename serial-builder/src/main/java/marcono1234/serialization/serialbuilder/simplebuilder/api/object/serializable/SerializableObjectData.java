package marcono1234.serialization.serialbuilder.simplebuilder.api.object.serializable;

import marcono1234.serialization.serialbuilder.builder.api.Enclosing;
import marcono1234.serialization.serialbuilder.builder.api.ThrowingConsumer;
import marcono1234.serialization.serialbuilder.builder.implementation.ClassTypeNameHelper;
import marcono1234.serialization.serialbuilder.simplebuilder.api.ObjectBuildingDataOutput;
import marcono1234.serialization.serialbuilder.simplebuilder.api.object.ObjectStart;

import java.io.ObjectOutputStream;
import java.util.function.Function;

public interface SerializableObjectData<C> extends SerializableObjectDataEnd<C> {
    // API Note: API does not group fields into primitive and object fields; implementation will take care of this

    /**
     * Writes a {@code boolean} field.
     *
     * @param fieldName
     *      name of the field
     * @param value
     *      value of the field
     * @return <i>this</i>
     */
    SerializableObjectData<C> primitiveBooleanField(String fieldName, boolean value);

    /**
     * Writes a {@code byte} field.
     *
     * @param fieldName
     *      name of the field
     * @param value
     *      value of the field
     * @return <i>this</i>
     */
    SerializableObjectData<C> primitiveByteField(String fieldName, byte value);

    /**
     * Writes a {@code char} field.
     *
     * @param fieldName
     *      name of the field
     * @param value
     *      value of the field
     * @return <i>this</i>
     */
    SerializableObjectData<C> primitiveCharField(String fieldName, char value);

    /**
     * Writes a {@code short} field.
     *
     * @param fieldName
     *      name of the field
     * @param value
     *      value of the field
     * @return <i>this</i>
     */
    SerializableObjectData<C> primitiveShortField(String fieldName, short value);

    /**
     * Writes an {@code int} field.
     *
     * @param fieldName
     *      name of the field
     * @param value
     *      value of the field
     * @return <i>this</i>
     */
    SerializableObjectData<C> primitiveIntField(String fieldName, int value);

    /**
     * Writes a {@code long} field.
     *
     * @param fieldName
     *      name of the field
     * @param value
     *      value of the field
     * @return <i>this</i>
     */
    SerializableObjectData<C> primitiveLongField(String fieldName, long value);

    /**
     * Writes a {@code float} field.
     *
     * @param fieldName
     *      name of the field
     * @param value
     *      value of the field
     * @return <i>this</i>
     */
    SerializableObjectData<C> primitiveFloatField(String fieldName, float value);

    /**
     * Writes a {@code double} field.
     *
     * @param fieldName
     *      name of the field
     * @param value
     *      value of the field
     * @return <i>this</i>
     */
    SerializableObjectData<C> primitiveDoubleField(String fieldName, double value);

    /**
     * Begins an object field.
     *
     * @param fieldName
     *      name of the field
     * @param fieldType
     *      type of the field in the form returned by {@link Class#getTypeName()},
     *      e.g. {@code java.util.Map$Entry} or {@code int[]}
     * @return <i>next step</i>
     */
    ObjectStart<SerializableObjectObjectFieldEnd<C>> beginObjectField(String fieldName, String fieldType);

    /**
     * Begins an object field.
     *
     * @param fieldName
     *      name of the field
     * @param fieldType
     *      type of the field
     * @return <i>next step</i>
     */
    default ObjectStart<SerializableObjectObjectFieldEnd<C>> beginObjectField(String fieldName, Class<?> fieldType) {
        return beginObjectField(fieldName, ClassTypeNameHelper.getObjectTypeName(fieldType));
    }

    /**
     * Writes an object field. Allows using a separate method for creating the field value without having to interrupt
     * the builder call chain. The writer function must call all builder methods and return the result of the last
     * builder method to make sure the data is written correctly.
     *
     * @param fieldName
     *      name of the field
     * @param fieldType
     *      type of the field in the form returned by {@link Class#getTypeName()},
     *      e.g. {@code java.util.Map$Entry} or {@code int[]}
     * @param writer
     *      for writing the value of the field
     * @return <i>this</i>
     */
    SerializableObjectDataEnd<C> objectField(String fieldName, String fieldType, Function<ObjectStart<Enclosing>, Enclosing> writer);

    /**
     * Writes an object field. Allows using a separate method for creating the field value without having to interrupt
     * the builder call chain. The writer function must call all builder methods and return the result of the last
     * builder method to make sure the data is written correctly.
     *
     * @param fieldName
     *      name of the field
     * @param fieldType
     *      type of the field
     * @param writer
     *      for writing the value of the field
     * @return <i>this</i>
     */
    default SerializableObjectDataEnd<C> objectField(String fieldName, Class<?> fieldType, Function<ObjectStart<Enclosing>, Enclosing> writer) {
        return objectField(fieldName, ClassTypeNameHelper.getObjectTypeName(fieldType), writer);
    }

    /**
     * Writes the data written by the special {@code writeObject} method (if any) of the class.
     * If the {@code writeObject} method calls {@link ObjectOutputStream#defaultWriteObject()} or
     * {@link ObjectOutputStream#writeFields()}, that data can be written by using the regular field methods of this
     * API.
     *
     * @return <i>next step</i>
     */
    SerializableObjectDataEnd<C> writeObjectWith(ThrowingConsumer<ObjectBuildingDataOutput> writer);
}
