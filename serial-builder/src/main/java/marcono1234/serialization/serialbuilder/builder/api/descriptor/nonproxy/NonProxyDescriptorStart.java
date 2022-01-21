package marcono1234.serialization.serialbuilder.builder.api.descriptor.nonproxy;

import marcono1234.serialization.serialbuilder.builder.implementation.SerialVersionUidHelper;

public interface NonProxyDescriptorStart<C> {
    /**
     * Sets the type name.
     *
     * @param name
     *      name of the type in the form returned by {@link Class#getTypeName()}, e.g.
     *      {@code java.util.Map$Entry} or {@code int[]}
     * @return <i>next step</i>
     */
    NonProxyDescriptorSerialVersionUid<C> typeName(String name);

    /**
     * Sets the type.
     *
     * @param c
     *      type which is represented by this descriptor
     * @return <i>next step</i>
     */
    default NonProxyDescriptorSerialVersionUid<C> type(Class<?> c) {
        return typeName(c.getTypeName());
    }

    /**
     * Sets the type and uses the JDK serialization code to determine the {@code serialVersionUID}
     * for the type.
     *
     * @param c
     *      type which is represented by this descriptor
     * @return <i>next step</i>
     */
    default NonProxyDescriptorFlags<C> typeWithUid(Class<?> c) {
        return type(c).uid(SerialVersionUidHelper.getSerialVersionUID(c));
    }

    /**
     * Sets the type to an enum class and sets the implicit {@code serialVersionUID} of 0.
     *
     * @param c
     *      type which is represented by this descriptor
     * @return <i>next step</i>
     */
    // Use raw type to allow Enum.class as well
    default NonProxyDescriptorFlags<C> enumClass(@SuppressWarnings("rawtypes") Class<? extends Enum> c) {
        // Enum uses 0 as UID
        return type(c).uid(0);
    }
}
