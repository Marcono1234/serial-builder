package marcono1234.serialization.serialbuilder.simplebuilder.api.object.serializable;

// Have a separate interface to only allow ending object when at least for one class
// data has been written
public interface SerializableObjectWithDataStart<C> extends SerializableObjectStart<C>, SerializableObjectEnd<C> {
}
