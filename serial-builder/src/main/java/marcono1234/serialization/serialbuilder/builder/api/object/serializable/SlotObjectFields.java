package marcono1234.serialization.serialbuilder.builder.api.object.serializable;

import marcono1234.serialization.serialbuilder.builder.api.object.ObjectStart;

public interface SlotObjectFields<C> extends ObjectStart<SlotObjectFields<C>> {
    /**
     * Ends the object field values.
     *
     * @return <i>next step</i>
     */
    SlotWriteObjectMethodData<C> endObjectFields();
}
