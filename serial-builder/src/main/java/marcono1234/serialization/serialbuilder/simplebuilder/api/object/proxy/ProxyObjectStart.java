package marcono1234.serialization.serialbuilder.simplebuilder.api.object.proxy;

import marcono1234.serialization.serialbuilder.builder.api.Handle;
import marcono1234.serialization.serialbuilder.builder.api.ThrowingConsumer;
import marcono1234.serialization.serialbuilder.builder.implementation.ClassTypeNameHelper;
import marcono1234.serialization.serialbuilder.builder.implementation.SerialVersionUidHelper;
import marcono1234.serialization.serialbuilder.simplebuilder.api.ObjectBuildingDataOutput;
import marcono1234.serialization.serialbuilder.simplebuilder.api.object.serializable.SerializableObjectStart;

import java.io.Externalizable;
import java.io.ObjectOutput;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

// Does not extend ObjectStart to not offer pointless objects, such as arrays or strings as invocation handler
public interface ProxyObjectStart<C> {
    /**
     * Writes a reference to an already written {@link InvocationHandler} object using an assigned handle.
     *
     * @param handle
     *      an assigned handle referencing a previously written {@code InvocationHandler} object
     * @return <i>next step</i>
     */
    C invocationHandlerHandle(Handle handle);

    /**
     * Begins a new {@link InvocationHandler} object which itself is a {@link Proxy}, and assigns a handle to it.
     *
     * @param unassignedHandle
     *      handle which should be assigned a reference to the written {@code InvocationHandler}
     * @param interfaceNames
     *      names of the interfaces implemented by the proxy, in the form returned by {@link Class#getTypeName()}, e.g.
     *      {@code java.util.Map$Entry}
     * @return <i>next step</i>
     */
    // Supports the case where invocation handler itself is a Proxy as well
    ProxyObjectStart<ProxyObjectEnd<C>> beginProxyInvocationHandler(Handle unassignedHandle, String... interfaceNames);

    /**
     * Begins a new {@link InvocationHandler} object which itself is a {@link Proxy}.
     *
     * @param interfaceNames
     *      names of the interfaces implemented by the proxy, in the form returned by {@link Class#getTypeName()}, e.g.
     *      {@code java.util.Map$Entry}
     * @return <i>next step</i>
     */
    default ProxyObjectStart<ProxyObjectEnd<C>> beginProxyInvocationHandler(String... interfaceNames) {
        return beginProxyInvocationHandler(new Handle(), interfaceNames);
    }

    /**
     * Begins a new {@link InvocationHandler} object which itself is a {@link Proxy}, and assigns a handle to it.
     *
     * @param unassignedHandle
     *      handle which should be assigned a reference to the written {@code InvocationHandler}
     * @param interfaces
     *      interfaces implemented by the proxy
     * @return <i>next step</i>
     */
    default ProxyObjectStart<ProxyObjectEnd<C>> beginProxyInvocationHandler(Handle unassignedHandle, Class<?>... interfaces) {
        return beginProxyInvocationHandler(unassignedHandle, ClassTypeNameHelper.getInterfaceNames(interfaces));
    }

    /**
     * Begins a new {@link InvocationHandler} object which itself is a {@link Proxy}.
     *
     * @param interfaces
     *      interfaces implemented by the proxy
     * @return <i>next step</i>
     */
    default ProxyObjectStart<ProxyObjectEnd<C>> beginProxyInvocationHandler(Class<?>... interfaces) {
        return beginProxyInvocationHandler(new Handle(), interfaces);
    }

    /**
     * Begins a new {@link InvocationHandler} object which implements {@link java.io.Serializable}, and assigns a handle to it.
     * The next step is the list of class data for the object, starting at the supertypes (if any) and ending with the
     * data for the class itself.
     *
     * @param unassignedHandle
     *      handle which should be assigned a reference to the written {@code InvocationHandler}
     * @return <i>next step</i>
     */
    SerializableObjectStart<C> beginSerializableInvocationHandler(Handle unassignedHandle);

    /**
     * Begins a new {@link InvocationHandler} object which implements {@link java.io.Serializable}.
     * The next step is the list of class data for the object, starting at the supertypes (if any) and ending with the
     * data for the class itself.
     *
     * @return <i>next step</i>
     */
    default SerializableObjectStart<C> beginSerializableInvocationHandler() {
        return beginSerializableInvocationHandler(new Handle());
    }

    /**
     * Writes a new {@link InvocationHandler} object which implements {@link java.io.Externalizable}, and assigns a handle to it.
     *
     * @param unassignedHandle
     *      handle which should be assigned a reference to the written {@code InvocationHandler}
     * @param typeName
     *      name of the {@code Externalizable} type, in the form returned by {@link Class#getTypeName()},
     *      e.g. {@code java.util.Map$Entry}
     * @param serialVersionUID
     *      value of the {@code serialVersionUID} field (or the calculated UID value)
     * @param writer
     *      for writing the data written by {@link java.io.Externalizable#writeExternal(ObjectOutput)}
     * @return <i>next step</i>
     */
    C externalizableInvocationHandler(Handle unassignedHandle, String typeName, long serialVersionUID, ThrowingConsumer<ObjectBuildingDataOutput> writer);

    /**
     * Writes a new {@link InvocationHandler} object which implements {@link java.io.Externalizable}.
     *
     * @param typeName
     *      name of the {@code Externalizable} type, in the form returned by {@link Class#getTypeName()},
     *      e.g. {@code java.util.Map$Entry}
     * @param serialVersionUID
     *      value of the {@code serialVersionUID} field (or the calculated UID value)
     * @param writer
     *      for writing the data written by {@link java.io.Externalizable#writeExternal(ObjectOutput)}
     * @return <i>next step</i>
     */
    default C externalizableInvocationHandler(String typeName, long serialVersionUID, ThrowingConsumer<ObjectBuildingDataOutput> writer) {
        return externalizableInvocationHandler(new Handle(), typeName, serialVersionUID, writer);
    }

    /**
     * Writes a new {@link InvocationHandler} object which implements {@link java.io.Externalizable}, and assigns a handle to it.
     *
     * @param unassignedHandle
     *      handle which should be assigned a reference to the written {@code InvocationHandler}
     * @param c
     *      the {@code Externalizable} type
     * @param writer
     *      for writing the data written by {@link java.io.Externalizable#writeExternal(ObjectOutput)}
     * @return <i>next step</i>
     */
    default C externalizableInvocationHandler(Handle unassignedHandle, Class<? extends Externalizable> c, ThrowingConsumer<ObjectBuildingDataOutput> writer) {
        return externalizableInvocationHandler(unassignedHandle, c.getTypeName(), SerialVersionUidHelper.getSerialVersionUID(c), writer);
    }

    /**
     * Writes a new {@link InvocationHandler} object which implements {@link java.io.Externalizable}.
     *
     * @param c
     *      the {@code Externalizable} type
     * @param writer
     *      for writing the data written by {@link java.io.Externalizable#writeExternal(ObjectOutput)}
     * @return <i>next step</i>
     */
    default C externalizableInvocationHandler(Class<? extends Externalizable> c, ThrowingConsumer<ObjectBuildingDataOutput> writer) {
        return externalizableInvocationHandler(new Handle(), c, writer);
    }
}
