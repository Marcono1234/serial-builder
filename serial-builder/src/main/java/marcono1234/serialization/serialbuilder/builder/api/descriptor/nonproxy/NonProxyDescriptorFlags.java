package marcono1234.serialization.serialbuilder.builder.api.descriptor.nonproxy;

public interface NonProxyDescriptorFlags<C> {
    /**
     * Sets the descriptor flags, see {@link java.io.ObjectStreamConstants} {@code SC_} constants.
     *
     * @return <i>next step</i>
     */
    NonProxyDescriptorPrimitiveFieldsStart<C> flags(int flags);
}
