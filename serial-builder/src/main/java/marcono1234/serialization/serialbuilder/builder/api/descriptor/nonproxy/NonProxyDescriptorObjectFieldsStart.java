package marcono1234.serialization.serialbuilder.builder.api.descriptor.nonproxy;

import marcono1234.serialization.serialbuilder.builder.api.descriptor.DescriptorEnd;

// Extend DescriptorEnd to allow skipping object fields
public interface NonProxyDescriptorObjectFieldsStart<C> extends DescriptorEnd<C> {
    /**
     * Begins the list of object field descriptors.
     *
     * @return <i>next step</i>
     */
    NonProxyDescriptorObjectFields<C> beginObjectFieldDescriptors();
}
