package marcono1234.serialization.serialbuilder.builder.api;

import marcono1234.serialization.serialbuilder.builder.api.object.ObjectStart;
import marcono1234.serialization.serialbuilder.builder.implementation.HandleAccess;

import java.util.Objects;

/**
 * A handle represents a reference to a previously written object in the stream.
 *
 * <h2>Usage</h2>
 * <ol>
 *     <li>{@linkplain #Handle() Create a new <code>Handle</code>}
 *     <li>Pass the handle to method with additional {@code Handle} parameter, e.g. {@link ObjectStart#beginExternalizableObject(Handle)}
 *     <li>Use the handle, e.g. with {@link ObjectStart#objectHandle(Handle)}
 * </ol>
 */
public class Handle {
    static {
        HandleAccess.INSTANCE = new HandleAccess() {
            @Override
            public void assignIndex(Handle unassignedHandle, int objectIndex) {
                if (unassignedHandle.isAssigned()) {
                    throw new IllegalStateException("Object index has already been assigned: " + unassignedHandle);
                }

                unassignedHandle.objectIndex = objectIndex;
            }

            @Override
            public int getObjectIndex(Handle assignedHandle) {
                if (!assignedHandle.isAssigned()) {
                    throw new IllegalStateException("Object index has not been assigned yet");
                }
                return assignedHandle.objectIndex;
            }
        };
    }

    private Integer objectIndex;

    /**
     * Creates a handle which has not been assigned an object index yet. The handle has to be passed to one of the
     * builder methods with {@code Handle} parameter to assign the index.
     */
    public Handle() {
        objectIndex = null;
    }

    /**
     * Returns whether this handle has been assigned an object index.
     */
    public boolean isAssigned() {
        return objectIndex != null;
    }

    @Override
    public String toString() {
        return "Handle{" +
            "objectIndex=" + Objects.toString(objectIndex, "?") +
            '}';
    }
}
