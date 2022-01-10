package marcono1234.serialization.serialbuilder.builder.api.descriptor.nonproxy;

import marcono1234.serialization.serialbuilder.builder.implementation.ClassTypeNameHelper;

public interface NonProxyDescriptorPrimitiveFields<C> {
    /**
     * Adds a primitive field.
     *
     * @param name
     *      name of the field
     * @param typeName
     *      name of the field type in the form returned by {@link Class#getTypeName()}, e.g.
     *      {@code java.util.Map$Entry} or {@code int[]}
     * @return <i>this</i>
     */
    NonProxyDescriptorPrimitiveFields<C> primitiveField(String name, String typeName);

    /**
     * Adds a primitive field.
     *
     * @param name
     *      name of the field
     * @param fieldType
     *      type of the field
     * @return <i>this</i>
     */
    default NonProxyDescriptorPrimitiveFields<C> primitiveField(String name, Class<?> fieldType) {
        return primitiveField(name, ClassTypeNameHelper.getPrimitiveTypeName(fieldType));
    }

    /**
     * Adds a {@code boolean} field.
     *
     * @param name
     *      name of the field
     * @return <i>this</i>
     */
    default NonProxyDescriptorPrimitiveFields<C> booleanField(String name) {
        return primitiveField(name, boolean.class);
    }

    /**
     * Adds a {@code byte} field.
     *
     * @param name
     *      name of the field
     * @return <i>this</i>
     */
    default NonProxyDescriptorPrimitiveFields<C> byteField(String name) {
        return primitiveField(name, byte.class);
    }

    /**
     * Adds a {@code char} field.
     *
     * @param name
     *      name of the field
     * @return <i>this</i>
     */
    default NonProxyDescriptorPrimitiveFields<C> charField(String name) {
        return primitiveField(name, char.class);
    }

    /**
     * Adds a {@code short} field.
     *
     * @param name
     *      name of the field
     * @return <i>this</i>
     */
    default NonProxyDescriptorPrimitiveFields<C> shortField(String name) {
        return primitiveField(name, short.class);
    }

    /**
     * Adds an {@code int} field.
     *
     * @param name
     *      name of the field
     * @return <i>this</i>
     */
    default NonProxyDescriptorPrimitiveFields<C> intField(String name) {
        return primitiveField(name, int.class);
    }

    /**
     * Adds a {@code long} field.
     *
     * @param name
     *      name of the field
     * @return <i>this</i>
     */
    default NonProxyDescriptorPrimitiveFields<C> longField(String name) {
        return primitiveField(name, long.class);
    }

    /**
     * Adds a {@code float} field.
     *
     * @param name
     *      name of the field
     * @return <i>this</i>
     */
    default NonProxyDescriptorPrimitiveFields<C> floatField(String name) {
        return primitiveField(name, float.class);
    }

    /**
     * Adds a {@code double} field.
     *
     * @param name
     *      name of the field
     * @return <i>this</i>
     */
    default NonProxyDescriptorPrimitiveFields<C> doubleField(String name) {
        return primitiveField(name, double.class);
    }

    /**
     * Ends the list of primitive field descriptors.
     *
     * @return <i>next step</i>
     */
    NonProxyDescriptorObjectFieldsStart<C> endPrimitiveFieldDescriptors();
}
