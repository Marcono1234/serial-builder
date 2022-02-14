package marcono1234.serialization.serialbuilder.simplebuilder.implementation;

import marcono1234.serialization.serialbuilder.SimpleSerialBuilder;
import marcono1234.serialization.serialbuilder.builder.api.Handle;
import marcono1234.serialization.serialbuilder.builder.api.ThrowingConsumer;
import marcono1234.serialization.serialbuilder.builder.implementation.SerialBuilderImpl;
import marcono1234.serialization.serialbuilder.simplebuilder.api.ObjectBuildingDataOutput;
import marcono1234.serialization.serialbuilder.simplebuilder.api.object.ObjectStart;
import marcono1234.serialization.serialbuilder.simplebuilder.api.object.proxy.ProxyObjectEnd;
import marcono1234.serialization.serialbuilder.simplebuilder.api.object.proxy.ProxyObjectStart;
import marcono1234.serialization.serialbuilder.simplebuilder.api.object.serializable.SerializableObjectStart;

public class SimpleSerialBuilderImpl extends DelegatingSimpleSerialBuilderImpl<byte[]> {
    public SimpleSerialBuilderImpl(marcono1234.serialization.serialbuilder.builder.api.object.ObjectStart<byte[]> delegateBuilder) {
        super(delegateBuilder);
    }

    @SuppressWarnings("unchecked")
    private static ObjectStart<byte[]> createStart() {
        return new SimpleSerialBuilderImpl(SerialBuilderImpl.createStart());
    }

    public static SimpleSerialBuilder.SerializableBuilderStart startSerializable(Handle unassignedHandle) {
        var delegate = createStart().beginSerializableObject(unassignedHandle);
        return delegate::beginClassData;
    }

    public static byte[] createExternalizable(Handle unassignedHandle, String typeName, long serialVersionUID, ThrowingConsumer<ObjectBuildingDataOutput> writer) {
        return createStart().externalizableObject(unassignedHandle, typeName, serialVersionUID, writer);
    }

    public static SimpleSerialBuilder.ProxyBuilderStart startProxy(Handle unassignedHandle, String... interfaceNames) {
        var delegate = createStart().beginProxyObject(unassignedHandle, interfaceNames);
        return new SimpleSerialBuilder.ProxyBuilderStart() {
            @Override
            public ProxyObjectEnd<byte[]> invocationHandlerHandle(Handle handle) {
                return delegate.invocationHandlerHandle(handle);
            }

            @Override
            public ProxyObjectStart<ProxyObjectEnd<ProxyObjectEnd<byte[]>>> beginProxyInvocationHandler(Handle unassignedHandle, String... interfaceNames) {
                return delegate.beginProxyInvocationHandler(unassignedHandle, interfaceNames);
            }

            @Override
            public SerializableObjectStart<ProxyObjectEnd<byte[]>> beginSerializableInvocationHandler(Handle unassignedHandle) {
                return delegate.beginSerializableInvocationHandler(unassignedHandle);
            }

            @Override
            public ProxyObjectEnd<byte[]> externalizableInvocationHandler(Handle unassignedHandle, String typeName, long serialVersionUID, ThrowingConsumer<ObjectBuildingDataOutput> writer) {
                return delegate.externalizableInvocationHandler(unassignedHandle, typeName, serialVersionUID, writer);
            }
        };
    }

    public static byte[] writeSerializationDataWith(ThrowingConsumer<ObjectBuildingDataOutput> writer) {
        return SerialBuilderImpl.writeSerializationDataWith(createDataOutputConsumer(null, writer));
    }
}
