package marcono1234.serialization.serialbuilder.builder.api.object.array;

public interface ArrayElements<C> {
    // TODO: Don't use varargs to avoid accidentally calling wrong overload (e.g. for byte or short)?

    /**
     * Writes the array elements.
     *
     * @return <i>next step</i>
     */
    ArrayEnd<C> elements(boolean... array);

    /**
     * Writes the array elements.
     *
     * @return <i>next step</i>
     */
    ArrayEnd<C> elements(byte... array);

    /**
     * Writes the array elements.
     *
     * @return <i>next step</i>
     */
    ArrayEnd<C> elements(char... array);

    /**
     * Writes the array elements.
     *
     * @return <i>next step</i>
     */
    ArrayEnd<C> elements(short... array);

    /**
     * Writes the array elements.
     *
     * @return <i>next step</i>
     */
    ArrayEnd<C> elements(int... array);

    /**
     * Writes the array elements.
     *
     * @return <i>next step</i>
     */
    ArrayEnd<C> elements(long... array);

    /**
     * Writes the array elements.
     *
     * @return <i>next step</i>
     */
    ArrayEnd<C> elements(float... array);

    /**
     * Writes the array elements.
     *
     * @return <i>next step</i>
     */
    ArrayEnd<C> elements(double... array);

    /**
     * Begins the object elements.
     *
     * @return <i>next step</i>
     */
    ArrayObjectElementsStart<C> beginObjectElements();
}
