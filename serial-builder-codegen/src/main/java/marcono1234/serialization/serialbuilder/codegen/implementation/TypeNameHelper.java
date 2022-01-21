package marcono1234.serialization.serialbuilder.codegen.implementation;

class TypeNameHelper {
    private TypeNameHelper() {
    }

    /**
     * Converts a type name to the form returned by {@link Class#getTypeName()}.
     * If {@code isJvmTypeName} is {@code true}, the given name is in the format used by the JVM.
     * Otherwise it is in the format returned by {@link Class#getName()}.
     */
    public static String createClassTypeName(String originalTypeName, boolean isJvmTypeName) {
        if (!isJvmTypeName && !originalTypeName.startsWith("[")) {
            return originalTypeName;
        }

        int elementTypeNameStart = 0;
        while (elementTypeNameStart < originalTypeName.length() && originalTypeName.charAt(elementTypeNameStart) == '[') {
            elementTypeNameStart++;
        }

        String elementTypeName = originalTypeName.substring(elementTypeNameStart);
        if (elementTypeName.isEmpty()) {
            throw new IllegalArgumentException("Missing element type name: " + originalTypeName);
        }

        String convertedElementTypeName = switch(elementTypeName) {
            case "B" -> "byte";
            case "C" -> "char";
            case "D" -> "double";
            case "F" -> "float";
            case "I" -> "int";
            case "J" -> "long";
            case "S" -> "short";
            case "Z" -> "boolean";
            default -> {
                if (!(elementTypeName.startsWith("L") && elementTypeName.endsWith(";"))) {
                    throw new IllegalArgumentException("Malformed object type name: " + elementTypeName);
                }
                // Remove 'L' and ';'
                String objectTypeName =  elementTypeName.substring(1, elementTypeName.length() - 1);
                if (isJvmTypeName) {
                    objectTypeName = objectTypeName.replace('/', '.');
                }
                yield objectTypeName;
            }
        };
        return convertedElementTypeName + "[]".repeat(elementTypeNameStart);
    }
}
