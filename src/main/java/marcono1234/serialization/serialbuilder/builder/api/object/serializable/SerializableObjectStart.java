package marcono1234.serialization.serialbuilder.builder.api.object.serializable;

import marcono1234.serialization.serialbuilder.builder.api.object.ObjectEnd;

// Extend ObjectEnd to allow skipping slots
public interface SerializableObjectStart<C> extends ObjectEnd<C> {
    /**
     * Begins the "slots" of the object, i.e. the object data. The slots have to match the fields written in
     * the descriptor. Slots are in reverse order compared to the descriptor hierarchy, they start with the
     * slots for the supertype and end with the slots for the subtype.
     *
     * @return <i>next step</i>
     */
    SlotsStart<C> beginSlots();
}
