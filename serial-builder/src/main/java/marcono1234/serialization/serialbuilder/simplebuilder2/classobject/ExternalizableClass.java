package marcono1234.serialization.serialbuilder.simplebuilder2.classobject;

import marcono1234.serialization.serialbuilder.simplebuilder.api.object.ObjectStart;

public record ExternalizableClass(String className, long serialVersionUID) implements ClassObject {
    @Override
    public <C> C buildSerialData(ObjectStart<C> objectStart) {
        return objectStart.externalizableClass(className, serialVersionUID);
    }
}
