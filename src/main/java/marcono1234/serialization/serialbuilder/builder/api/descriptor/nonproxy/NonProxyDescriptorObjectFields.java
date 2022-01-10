package marcono1234.serialization.serialbuilder.builder.api.descriptor.nonproxy;

import marcono1234.serialization.serialbuilder.builder.api.descriptor.DescriptorEnd;
import marcono1234.serialization.serialbuilder.builder.implementation.ClassTypeNameHelper;

public interface NonProxyDescriptorObjectFields<C> {
    /**
     * Adds an object field.
     *
     * @param name
     *      name of the field
     * @param typeName
     *      name of the field type in the form returned by {@link Class#getTypeName()}, e.g.
     *      {@code java.util.Map$Entry} or {@code int[]}
     * @return <i>this</i>
     */
    NonProxyDescriptorObjectFields<C> objectField(String name, String typeName);

    /**
     * Adds an object field.
     *
     * @param name
     *      name of the field
     * @param fieldType
     *      type of the field
     * @return <i>this</i>
     */
    default NonProxyDescriptorObjectFields<C> objectField(String name, Class<?> fieldType) {
        return objectField(name, ClassTypeNameHelper.getObjectTypeName(fieldType));
    }

    /**
     * Ends the list of object field descriptors.
     *
     * @return <i>next step</i>
     */
    DescriptorEnd<C> endObjectFieldDescriptors();
}
