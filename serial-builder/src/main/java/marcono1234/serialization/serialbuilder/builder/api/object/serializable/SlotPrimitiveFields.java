package marcono1234.serialization.serialbuilder.builder.api.object.serializable;

public interface SlotPrimitiveFields<C> {
    // TODO: Choose different names for primitive field methods to avoid accidentally calling wrong overload (e.g. for byte or short)?

    /**
     * Writes a primitive field value.
     *
     * @return <i>this</i>
     */
    SlotPrimitiveFields<C> value(boolean b);

    /**
     * Writes a primitive field value.
     *
     * @return <i>this</i>
     */
    SlotPrimitiveFields<C> value(byte b);

    /**
     * Writes a primitive field value.
     *
     * @return <i>this</i>
     */
    SlotPrimitiveFields<C> value(char c);

    /**
     * Writes a primitive field value.
     *
     * @return <i>this</i>
     */
    SlotPrimitiveFields<C> value(short s);

    /**
     * Writes a primitive field value.
     *
     * @return <i>this</i>
     */
    SlotPrimitiveFields<C> value(int i);

    /**
     * Writes a primitive field value.
     *
     * @return <i>this</i>
     */
    SlotPrimitiveFields<C> value(long l);

    /**
     * Writes a primitive field value.
     *
     * @return <i>this</i>
     */
    SlotPrimitiveFields<C> value(float f);

    /**
     * Writes a primitive field value.
     *
     * @return <i>this</i>
     */
    SlotPrimitiveFields<C> value(double d);

    /**
     * Ends the primitive field values.
     *
     * @return <i>next step</i>
     */
    SlotObjectFieldsStart<C> endPrimitiveFields();
}
