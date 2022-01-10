package marcono1234.serialization.serialbuilder.builder.api.object;

public interface EnumStart<C> {
    /**
     * Sets the enum constant name.
     *
     * @return <i>next step</i>
     */
    EnumEnd<C> name(String constantName);
}
