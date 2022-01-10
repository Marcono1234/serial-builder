package marcono1234.serialization.serialbuilder.builder.api.descriptor;

import marcono1234.serialization.serialbuilder.builder.api.Handle;

public interface DescriptorsList<C> extends DescriptorStart<DescriptorsList<C>> {
    /**
     * Writes a handle to a descriptor and ends the descriptor hierarchy.
     *
     * @param handle
     *      an already assigned handle
     * @return <i>next step</i>
     */
    C endDescriptorHierarchyWithHandle(Handle handle);

    /**
     * Ends the descriptor hierarchy.
     *
     * @return <i>next step</i>
     */
    C endDescriptorHierarchy();
}
