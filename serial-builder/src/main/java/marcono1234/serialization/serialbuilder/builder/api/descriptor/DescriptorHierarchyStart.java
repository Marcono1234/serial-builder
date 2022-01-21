package marcono1234.serialization.serialbuilder.builder.api.descriptor;

import marcono1234.serialization.serialbuilder.builder.api.Enclosing;

import java.util.function.Function;

public interface DescriptorHierarchyStart<C> {
    /**
     * Begins the descriptor hierarchy. The hierarchy starts at the serialized class followed by its superclass (if any),
     * and so on. This differs from the class data (written later after the hierarchy) which is in reverse order.
     *
     * @return <i>next step</i>
     */
    DescriptorsList<C> beginDescriptorHierarchy();

    /**
     * Writes the descriptor hierarchy. Allows using a separate method for creating the descriptor hierarchy
     * without having to interrupt the builder call chain. The writer function must call all builder methods and
     * return the result of the last builder method to make sure the data is written correctly.
     *
     * @return <i>next step</i>
     */
    // Note: Function instead of Consumer is used as parameter type to force user to make all necessary calls
    // and return result of last call
    C descriptorHierarchy(Function<DescriptorsList<Enclosing>, Enclosing> writer);
}
