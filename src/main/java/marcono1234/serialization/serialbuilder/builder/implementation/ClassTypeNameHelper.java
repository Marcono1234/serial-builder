package marcono1234.serialization.serialbuilder.builder.implementation;

public class ClassTypeNameHelper {
    private ClassTypeNameHelper() {
    }

    public static String getPrimitiveTypeName(Class<?> fieldType) {
        if (!fieldType.isPrimitive()) {
            throw new IllegalArgumentException("Not a primitive type: " + fieldType);
        }
        return fieldType.getTypeName();
    }

    public static String getObjectTypeName(Class<?> fieldType) {
        if (fieldType.isPrimitive()) {
            throw new IllegalArgumentException("Not an Object type: " + fieldType);
        }
        return fieldType.getTypeName();
    }

    public static String[] getInterfaceNames(Class<?>... interfaces) {
        String[] interfacesNames = new String[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            Class<?> interface_ = interfaces[i];
            if (!interface_.isInterface()) {
                throw new IllegalArgumentException("Not an interface: " + interface_);
            }

            interfacesNames[i] = interfaces[i].getTypeName();
        }
        return interfacesNames;
    }
}
