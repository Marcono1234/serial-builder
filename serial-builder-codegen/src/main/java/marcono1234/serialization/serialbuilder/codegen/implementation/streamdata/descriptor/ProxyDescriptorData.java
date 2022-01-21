package marcono1234.serialization.serialbuilder.codegen.implementation.streamdata.descriptor;

import java.util.List;
import java.util.Optional;

public record ProxyDescriptorData(
    /** Interface names in the form returned by {@link Class#getTypeName()} */
    List<String> interfaceNames,
    Optional<NonProxyDescriptorData> superClassDesc,
    boolean isSuperClassDescHandle
) implements DescriptorStreamObject {
    /**
     * Whether a handle was used to refer to the super class, or the super class uses a handle
     */
    public boolean usesAnyHandle() {
        return isSuperClassDescHandle || superClassDesc.map(NonProxyDescriptorData::usesAnyHandle).orElse(false);
    }
}
