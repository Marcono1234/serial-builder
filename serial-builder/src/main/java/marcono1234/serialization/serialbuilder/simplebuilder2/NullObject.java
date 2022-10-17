package marcono1234.serialization.serialbuilder.simplebuilder2;

import marcono1234.serialization.serialbuilder.simplebuilder.api.object.ObjectStart;

public final class NullObject implements ObjectBase {
    private NullObject() {}

    public static final NullObject INSTANCE = new NullObject();

    @Override
    public <C> C buildSerialData(ObjectStart<C> objectStart) {
        return objectStart.nullObject();
    }
}
