package marcono1234.serialization.serialbuilder.builder.api.descriptor.nonproxy;

import marcono1234.serialization.serialbuilder.builder.api.descriptor.DescriptorEnd;

// Extend NonProxyDescriptorObjectFieldsStart to allow skipping primitive fields
// Extend DescriptorEnd to allow skipping primitive and object fields
public interface NonProxyDescriptorPrimitiveFieldsStart<C> extends NonProxyDescriptorObjectFieldsStart<C>, DescriptorEnd<C> {
    /**
     * Begins the list of primitive field descriptors.
     *
     * @return <i>next step</i>
     */
    NonProxyDescriptorPrimitiveFields<C> beginPrimitiveFieldDescriptors();
}
