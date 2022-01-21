package marcono1234.serialization.serialbuilder.builder.api.object.serializable;

// Extend SlotObjectFieldsStart to allow skipping primitive fields
// Extend SlotWriteObjectMethodData to allow skipping primitive and object fields
public interface SlotStart<C> extends SlotObjectFieldsStart<C>, SlotWriteObjectMethodData<C> {
    /**
     * Begins the primitive field values. The field values should be in the same order as the field entries of the
     * descriptor, which were written in a previous step.
     *
     * @return <i>next step</i>
     */
    SlotPrimitiveFields<C> beginPrimitiveFields();
}
