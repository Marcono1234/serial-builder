package marcono1234.serialization.serialbuilder.builder.implementation;

import java.util.Arrays;
import java.util.List;

public class ClassTypeNameHelper {
    private ClassTypeNameHelper() {
    }

    public static String getPrimitiveTypeName(Class<?> fieldType) {
        if (!fieldType.isPrimitive()) {
            throw new IllegalArgumentException("Not a primitive type: " + fieldType.getTypeName());
        }
        return fieldType.getTypeName();
    }

    public static String getObjectTypeName(Class<?> fieldType) {
        if (fieldType.isPrimitive()) {
            throw new IllegalArgumentException("Not an Object type: " + fieldType.getTypeName());
        }
        return fieldType.getTypeName();
    }

    public static String[] getInterfaceNames(Class<?>... interfaces) {
        return getInterfaceNames(Arrays.asList(interfaces));
    }

    public static String[] getInterfaceNames(List<Class<?>> interfaces) {
        return interfaces.stream()
            .map(c -> {
                if (!c.isInterface()) {
                    throw new IllegalArgumentException("Not an interface: " + c.getTypeName());
                }
                return c.getTypeName();
            })
            .toArray(String[]::new);
    }
}
