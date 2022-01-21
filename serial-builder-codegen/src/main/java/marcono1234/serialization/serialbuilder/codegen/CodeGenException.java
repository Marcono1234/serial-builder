package marcono1234.serialization.serialbuilder.codegen;

/**
 * Thrown when code generation fails. This can happen when the serialization data is malformed, or when it cannot
 * be recreated using the builder API methods.
 */
public class CodeGenException extends Exception {
    public CodeGenException(String message) {
        super(message);
    }

    public CodeGenException(String message, Throwable cause) {
        super(message, cause);
    }
}
