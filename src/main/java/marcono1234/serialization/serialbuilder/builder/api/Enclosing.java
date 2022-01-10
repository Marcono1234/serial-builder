package marcono1234.serialization.serialbuilder.builder.api;

import java.util.function.Function;

/**
 * Dummy interface to represent an abstract enclosing context for builder methods with {@link Function} parameter.
 * This has the following advantages:
 * <ul>
 *     <li>It requires the implementation of the {@code Function} to call all necessary builders steps to be able
 *     to return the correct type
 *     <li>It allows reusing the same {@code Function} implementation, regardless of the current context within
 *     the builder call chain
 * </ul>
 */
public interface Enclosing {
}
