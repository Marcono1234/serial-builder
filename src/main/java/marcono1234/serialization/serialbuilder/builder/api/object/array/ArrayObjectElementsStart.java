package marcono1234.serialization.serialbuilder.builder.api.object.array;

import marcono1234.serialization.serialbuilder.builder.api.object.ObjectStart;

public interface ArrayObjectElementsStart<C> extends ObjectStart<ArrayObjectElementsStart<C>> {
    /**
     * Ends the array elements.
     *
     * @return <i>next step</i>
     */
    ArrayEnd<C> endElements();
}
