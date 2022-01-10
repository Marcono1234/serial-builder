package marcono1234.serialization.serialbuilder.builder.api.descriptor.nonproxy;

public interface NonProxyDescriptorSerialVersionUid<C> {
    /**
     * Sets the {@code serialVersionUID}.
     *
     * @return <i>next step</i>
     */
    NonProxyDescriptorFlags<C> uid(long uid);
}
