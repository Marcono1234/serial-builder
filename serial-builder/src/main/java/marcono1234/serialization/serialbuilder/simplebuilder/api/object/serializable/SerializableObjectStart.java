package marcono1234.serialization.serialbuilder.simplebuilder.api.object.serializable;

import marcono1234.serialization.serialbuilder.builder.implementation.SerialVersionUidHelper;

import java.io.Serializable;

public interface SerializableObjectStart<C> {
    /**
     * Begins the data for the class in the hierarchy of the object.
     *
     * @param className
     *      name of the class in the form returned by {@link Class#getTypeName()}, e.g.
     *      {@code java.util.Map$Entry}
     * @param serialVersionUID
     *      {@code serialVersionUID} of the class
     * @return <i>next step</i>
     */
    SerializableObjectData<SerializableObjectWithDataStart<C>> beginClassData(String className, long serialVersionUID);

    /**
     * Begins the data for the class in the hierarchy of the object.
     *
     * @return <i>next step</i>
     */
    default SerializableObjectData<SerializableObjectWithDataStart<C>> beginClassData(Class<? extends Serializable> c) {
        return beginClassData(c.getTypeName(), SerialVersionUidHelper.getSerialVersionUID(c));
    }
}
