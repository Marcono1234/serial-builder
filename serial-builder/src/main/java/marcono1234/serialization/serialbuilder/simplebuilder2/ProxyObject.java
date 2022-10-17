package marcono1234.serialization.serialbuilder.simplebuilder2;

import marcono1234.serialization.serialbuilder.simplebuilder.api.object.ObjectStart;

import java.util.List;

public record ProxyObject(
    List<String> interfaceNames,
    ProxyInvocationHandlerObject invocationHandler
) implements ProxyInvocationHandlerObject {
    @Override
    public <C> C buildSerialData(ObjectStart<C> objectStart) {
        var start = objectStart.beginProxyObject(interfaceNames.toArray(String[]::new));

        // TODO: This is not safe; just happens to work due to how the implementation works; because ProxyObjectStart
        //       uses dedicated object creation methods, cannot pass it to buildSerialData
        invocationHandler.buildSerialData(objectStart);
        return (C) start;
    }
}
