package marcono1234.serialization.serialbuilder.builder.implementation;

import marcono1234.serialization.serialbuilder.builder.api.Handle;

/**
 * Implementation class which allows setting and getting the object index of a {@link Handle}.
 *
 * <p>Based on JDK's {@code jdk.internal.access.SharedSecrets}.
 */
public abstract class HandleAccess {
    public static volatile HandleAccess INSTANCE;

    static {
        // Perform any operation to load Handle class and assign INSTANCE
        new Handle();
    }

    public abstract void assignIndex(Handle unassignedHandle, int objectIndex);
    public abstract int getObjectIndex(Handle assignedHandle);
}
