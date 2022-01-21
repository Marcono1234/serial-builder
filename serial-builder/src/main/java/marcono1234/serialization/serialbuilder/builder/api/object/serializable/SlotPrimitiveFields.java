package marcono1234.serialization.serialbuilder.builder.api.object.serializable;

public interface SlotPrimitiveFields<C> {
    /**
     * Writes a {@code boolean} field value.
     *
     * @return <i>this</i>
     */
    SlotPrimitiveFields<C> booleanValue(boolean b);

    /**
     * Writes a {@code byte} field value.
     *
     * @return <i>this</i>
     */
    SlotPrimitiveFields<C> byteValue(byte b);

    /**
     * Writes a {@code char} field value.
     *
     * @return <i>this</i>
     */
    SlotPrimitiveFields<C> charValue(char c);

    /**
     * Writes a {@code short} field value.
     *
     * @return <i>this</i>
     */
    SlotPrimitiveFields<C> shortValue(short s);

    /**
     * Writes an {@code int} field value.
     *
     * @return <i>this</i>
     */
    SlotPrimitiveFields<C> intValue(int i);

    /**
     * Writes a {@code long} field value.
     *
     * @return <i>this</i>
     */
    SlotPrimitiveFields<C> longValue(long l);

    /**
     * Writes a {@code float} field value.
     *
     * @return <i>this</i>
     */
    SlotPrimitiveFields<C> floatValue(float f);

    /**
     * Writes a {@code double} field value.
     *
     * @return <i>this</i>
     */
    SlotPrimitiveFields<C> doubleValue(double d);

    /**
     * Ends the primitive field values.
     *
     * @return <i>next step</i>
     */
    SlotObjectFieldsStart<C> endPrimitiveFields();
}
