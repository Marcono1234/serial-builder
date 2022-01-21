package marcono1234.serialization.serialbuilder.codegen.implementation.streamdata.array;

import marcono1234.serialization.serialbuilder.codegen.implementation.writer.LiteralsHelper;

import java.lang.reflect.Array;

public class PrimitiveArrayHelper {
    private PrimitiveArrayHelper() {
    }

    public static String createArrayCode(Object array) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("new ");
        stringBuilder.append(array.getClass().getComponentType().getName());
        stringBuilder.append("[] {");

        int length = Array.getLength(array);
        for (int i = 0; i < length; i++) {
            Object element = Array.get(array, i);
            stringBuilder.append(LiteralsHelper.primitiveToString(element));

            if (i < length - 1) {
                stringBuilder.append(", ");
            }
        }

        stringBuilder.append('}');
        return stringBuilder.toString();
    }
}
