package marcono1234.serialization.serialbuilder.builder.api.object.externalizable;

import marcono1234.serialization.serialbuilder.builder.api.ObjectBuildingDataOutput;
import marcono1234.serialization.serialbuilder.builder.api.ThrowingConsumer;
import marcono1234.serialization.serialbuilder.builder.api.object.ObjectEnd;

import java.io.ObjectOutput;

public interface ExternalizableObjectStart<C> {
    /**
     * Writes the data written by the {@link java.io.Externalizable#writeExternal(ObjectOutput)} method.
     *
     * @return <i>next step</i>
     */
    ObjectEnd<C> writeExternalWith(ThrowingConsumer<ObjectBuildingDataOutput> writer);
}
