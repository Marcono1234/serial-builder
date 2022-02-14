package marcono1234.serialization.serialbuilder.builder.implementation;

import java.io.ObjectStreamClass;
import java.util.Objects;

public class SerialVersionUidHelper {
    private SerialVersionUidHelper() { }

    public static long getSerialVersionUID(Class<?> c) {
        Objects.requireNonNull(c);
        ObjectStreamClass objectStreamClass = ObjectStreamClass.lookup(c);
        if (objectStreamClass == null) {
            throw new IllegalArgumentException("Not Serializable: " + c.getTypeName());
        }
        return objectStreamClass.getSerialVersionUID();
    }
}
