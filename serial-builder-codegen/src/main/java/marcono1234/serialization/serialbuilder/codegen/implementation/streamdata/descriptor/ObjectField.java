package marcono1234.serialization.serialbuilder.codegen.implementation.streamdata.descriptor;

public record ObjectField(
    String name,
    /** Type name in the form returned by {@link Class#getTypeName()} */
    String typeName,
    /** Whether a handle was used for the field type name */
    boolean usesTypeNameHandle
) {
}
