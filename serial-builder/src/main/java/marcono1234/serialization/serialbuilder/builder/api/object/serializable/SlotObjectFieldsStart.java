package marcono1234.serialization.serialbuilder.builder.api.object.serializable;

// Extend SlotWriteObjectMethodData to allow skipping object fields
public interface SlotObjectFieldsStart<C> extends SlotWriteObjectMethodData<C> {
    /**
     * Begins the object field values. The field values should be in the same order as the field entries of the
     * descriptor, which were written in a previous step.
     *
     * @return <i>next step</i>
     */
    SlotObjectFields<C> beginObjectFields();
}
