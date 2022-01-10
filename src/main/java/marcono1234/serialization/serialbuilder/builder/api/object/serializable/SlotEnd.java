package marcono1234.serialization.serialbuilder.builder.api.object.serializable;

public interface SlotEnd<C> {
    /**
     * Ends the slot.
     *
     * @return <i>next step</i>
     */
    // Wrap with SlotsStart to allow multiple slots
    SlotsStart<C> endSlot();
}
