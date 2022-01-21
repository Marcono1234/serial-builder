package marcono1234.serialization.serialbuilder.simplebuilder.api;

import marcono1234.serialization.serialbuilder.SimpleSerialBuilder;
import marcono1234.serialization.serialbuilder.builder.api.Handle;
import marcono1234.serialization.serialbuilder.simplebuilder.api.object.ObjectStart;

import java.io.DataOutput;
import java.io.IOException;

/**
 * {@link DataOutput} which also has builder methods for creating serialized objects. It is also possible to
 * write references (in the form of {@link Handle} instances) to data previously written by the {@link SimpleSerialBuilder}.
 *
 * <p><b>Important:</b> When using the object builder methods, care must be taken to call all builder methods
 * (until the return type is {@code Void}) to make sure the object is properly constructed.
 */
public interface ObjectBuildingDataOutput extends DataOutput, ObjectStart<Void> {
    /**
     * @deprecated
     *      This method is error-prone because it discards the high 8 bits of each char.
     */
    @Deprecated
    @Override
    void writeBytes(String s) throws IOException;
}
