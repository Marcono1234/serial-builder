package marcono1234.serialization.serialbuilder.codegen.implementation.streamdata.descriptor;

import java.util.List;
import java.util.Optional;

public record NonProxyDescriptorData(
    /** Type name in the form returned by {@link Class#getTypeName()} */
    String typeName,
    long serialVersionUid,
    byte flags,
    List<PrimitiveField> primitiveFields,
    List<ObjectField> objectFields,
    Optional<NonProxyDescriptorData> superClassDesc,
    boolean superClassDescUsesHandle
) implements DescriptorStreamObject {
    /**
     * Whether any of the object fields use a handle for the field type name, or whether the
     * super class reference is a handle
     */
    public boolean usesAnyHandle() {
        return superClassDescUsesHandle || objectFields.stream().anyMatch(ObjectField::usesTypeNameHandle);
    }
}
