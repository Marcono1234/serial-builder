package marcono1234.serialization.serialbuilder.codegen.implementation.writer;

public class CodeWriter {
    private final boolean writeComments;
    private final boolean writeUnsupportedHandleComments;
    private final StringBuilder codeStringBuilder;
    private int indentationLevel;

    public CodeWriter(boolean writeComments, boolean writeUnsupportedHandleComments) {
        this.writeComments = writeComments;
        this.writeUnsupportedHandleComments = writeUnsupportedHandleComments;

        codeStringBuilder = new StringBuilder();
        indentationLevel = 0;
    }

    /**
     * Creates a new empty copy of this writer with the same settings and same indentation.
     */
    public CodeWriter(CodeWriter other) {
        this.writeComments = other.writeComments;
        this.writeUnsupportedHandleComments = other.writeUnsupportedHandleComments;
        this.indentationLevel = other.indentationLevel;
        this.codeStringBuilder = new StringBuilder();
    }

    public void increaseIndentation() {
        indentationLevel++;
    }

    public void decreaseIndentation() {
        if (indentationLevel <= 0) {
            throw new IllegalStateException("Code is not indented");
        }
        indentationLevel--;
    }

    public void writeLine(String line) {
        codeStringBuilder.append("    ".repeat(indentationLevel));
        codeStringBuilder.append(line);
        codeStringBuilder.append('\n');
    }

    private static boolean isVisibleAscii(int c) {
        return c >= ' ' && c <= '~';
    }

    public void writeComment(String comment) {
        // Validate comment to prevent creating malformed code
        if (!comment.chars().allMatch(CodeWriter::isVisibleAscii)) {
            throw new IllegalArgumentException("Unsupported comment: " + comment);
        }

        if (writeComments) {
            writeLine("// " + comment);
        }
    }

    /**
     * Writes a comment indicating that the original serialization data used a handle
     * whose usage is not supported by the builder API.
     */
    public void writeUnsupportedHandleUsageComment(String usageDescription) {
        if (writeUnsupportedHandleComments) {
            writeComment("Unsupported handle usage in serial data: " + usageDescription);
        }
    }

    /**
     * Gets the generated code and verifies that the current indentation level is 0.
     */
    public String getCode() {
        return getCode(true);
    }

    /**
     * Gets the generated code, optionally checking if the indentation is 0 to verify
     * correctly generated code.
     */
    public String getCode(boolean checkIndentationLevel) {
        if (checkIndentationLevel && indentationLevel != 0) {
            throw new IllegalStateException("Unexpected indentation: " + indentationLevel);
        }
        return codeStringBuilder.toString();
    }
}
