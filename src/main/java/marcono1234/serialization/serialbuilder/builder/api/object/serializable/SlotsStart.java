package marcono1234.serialization.serialbuilder.builder.api.object.serializable;

import marcono1234.serialization.serialbuilder.builder.api.Enclosing;
import marcono1234.serialization.serialbuilder.builder.api.object.ObjectEnd;

import java.util.function.Function;

public interface SlotsStart<C> {
    /**
     * Begins a slot.
     *
     * @return <i>next step</i>
     */
    SlotStart<C> beginSlot();

    /**
     * Ends the slots.
     *
     * @return <i>next step</i>
     */
    ObjectEnd<C> endSlots();

    /**
     * Writes a slot. Allows using a separate method for creating the slot without having to interrupt the
     * builder call chain. The writer function must call all builder methods and return the result of the last
     * builder method to make sure the data is written correctly.
     *
     * @return <i>next step</i>
     */
    // Note: Function instead of Consumer is used as parameter type to force user to make all necessary calls
    // and return result of last call
    @SuppressWarnings("unchecked")
    default SlotsStart<C> slot(Function<SlotStart<Enclosing>, SlotsStart<Enclosing>> writer) {
        return (SlotsStart<C>) writer.apply((SlotStart<Enclosing>) beginSlot());
    }
}
