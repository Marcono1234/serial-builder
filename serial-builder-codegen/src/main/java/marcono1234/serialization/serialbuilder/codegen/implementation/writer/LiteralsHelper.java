package marcono1234.serialization.serialbuilder.codegen.implementation.writer;

import java.util.HexFormat;

public class LiteralsHelper {
    private LiteralsHelper() {
    }

    private static final HexFormat hexFormat = HexFormat.of();

    private static void appendLiteralChar(StringBuilder stringBuilder, char c, boolean isCharLiteral) {
        if (c == '\\') {
            stringBuilder.append("\\\\");
        } else if (c == '\b') {
            stringBuilder.append("\\b");
        } else if (c == '\t') {
            stringBuilder.append("\\t");
        } else if (c == '\n') {
            stringBuilder.append("\\n");
        } else if (c == '\f') {
            stringBuilder.append("\\f");
        } else if (c == '\r') {
            stringBuilder.append("\\r");
        }
        // ' inside char literal
        else if (isCharLiteral && c == '\'') {
            stringBuilder.append("\\'");
        }
        // " inside String literal
        else if (!isCharLiteral && c == '"') {
            stringBuilder.append("\\\"");
        }
        // Include visible ASCII chars as they are
        else if (c >= ' ' && c <= '~') {
            stringBuilder.append(c);
        }
        // Encode using Unicode-escape
        else {
            stringBuilder.append("\\u");
            stringBuilder.append(hexFormat.toHexDigits(c));
        }
    }

    public static String createStringLiteral(String value) {
        StringBuilder stringBuilder = new StringBuilder(2 + value.length());
        stringBuilder.append('"');
        for (char c : value.toCharArray()) {
            appendLiteralChar(stringBuilder, c, false);
        }
        stringBuilder.append('"');
        return stringBuilder.toString();
    }

    public static String createCharLiteral(char c) {
        StringBuilder stringBuilder = new StringBuilder(3);
        stringBuilder.append('\'');
        appendLiteralChar(stringBuilder, c, true);
        stringBuilder.append('\'');
        return stringBuilder.toString();
    }

    public static String createLongLiteral(long l) {
        return l + "L";
    }

    public static String createFloatLiteral(float f) {
        if (Float.isNaN(f)) {
            return "Float.NaN";
        } else if (f == Float.NEGATIVE_INFINITY) {
            return "Float.NEGATIVE_INFINITY";
        } else if (f == Float.POSITIVE_INFINITY) {
            return "Float.POSITIVE_INFINITY";
        }
        return f + "f";
    }

    public static String createDoubleLiteral(double d) {
        if (Double.isNaN(d)) {
            return "Double.NaN";
        } else if (d == Double.NEGATIVE_INFINITY) {
            return "Double.NEGATIVE_INFINITY";
        } else if (d == Double.POSITIVE_INFINITY) {
            return "Double.POSITIVE_INFINITY";
        }
        return Double.toString(d);
    }

    public static String primitiveToString(Object primitiveValue) {
        if (primitiveValue instanceof Character c) {
            return LiteralsHelper.createCharLiteral(c);
        } else if (primitiveValue instanceof Long l) {
            return LiteralsHelper.createLongLiteral(l);
        } else if (primitiveValue instanceof Float f) {
            return LiteralsHelper.createFloatLiteral(f);
        } else if (primitiveValue instanceof Double d) {
            return LiteralsHelper.createDoubleLiteral(d);
        } else {
            return primitiveValue.toString();
        }
    }
}
