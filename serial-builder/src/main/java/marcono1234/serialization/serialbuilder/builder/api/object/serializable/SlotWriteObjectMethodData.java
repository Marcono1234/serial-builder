package marcono1234.serialization.serialbuilder.builder.api.object.serializable;

import marcono1234.serialization.serialbuilder.builder.api.ObjectBuildingDataOutput;
import marcono1234.serialization.serialbuilder.builder.api.ThrowingConsumer;

import java.io.ObjectOutputStream;

// Represents data written by `writeObject` of a class
// This interface is modeled as part of a slot (with optional fields before) to allow
// representing the case where `writeObject` calls `defaultWriteObject()`
// Extend SlotEnd to allow skipping `writeObject` data
public interface SlotWriteObjectMethodData<C> extends SlotEnd<C> {
    /**
     * Writes the data written by the special {@code writeObject} method (if any) of the class.
     * If the {@code writeObject} method calls {@link ObjectOutputStream#defaultWriteObject()} or
     * {@link ObjectOutputStream#writeFields()}, that data can be written by using the regular field methods of this
     * API.
     *
     * @return <i>next step</i>
     */
    SlotEnd<C> writeObjectWith(ThrowingConsumer<ObjectBuildingDataOutput> writer);
}
