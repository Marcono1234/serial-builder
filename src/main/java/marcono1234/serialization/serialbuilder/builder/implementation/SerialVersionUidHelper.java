package marcono1234.serialization.serialbuilder.builder.implementation;

import java.io.ObjectStreamClass;

public class SerialVersionUidHelper {
    private SerialVersionUidHelper() { }

    public static long getSerialVersionUID(Class<?> c) {
        ObjectStreamClass objectStreamClass = ObjectStreamClass.lookup(c);
        if (objectStreamClass == null) {
            throw new IllegalArgumentException("Not Serializable: " + c);
        }
        return objectStreamClass.getSerialVersionUID();
    }
}
