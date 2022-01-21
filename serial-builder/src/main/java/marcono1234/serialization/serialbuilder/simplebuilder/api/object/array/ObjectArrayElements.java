package marcono1234.serialization.serialbuilder.simplebuilder.api.object.array;

import marcono1234.serialization.serialbuilder.simplebuilder.api.object.ObjectStart;

public interface ObjectArrayElements<C> extends ObjectStart<ObjectArrayElements<C>> {
    /**
     * Ends the array.
     *
     * @return <i>next step</i>
     */
    C endArray();
}
