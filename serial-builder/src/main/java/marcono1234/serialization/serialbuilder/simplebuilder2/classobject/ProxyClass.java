package marcono1234.serialization.serialbuilder.simplebuilder2.classobject;

import marcono1234.serialization.serialbuilder.simplebuilder.api.object.ObjectStart;

import java.util.List;

public record ProxyClass(List<String> interfaceNames) implements ClassObject {
    @Override
    public <C> C buildSerialData(ObjectStart<C> objectStart) {
        return objectStart.proxyClass(interfaceNames.toArray(String[]::new));
    }
}
