package marcono1234.serialization.serialbuilder.builder.api.descriptor;

import marcono1234.serialization.serialbuilder.builder.api.Enclosing;
import marcono1234.serialization.serialbuilder.builder.api.Handle;
import marcono1234.serialization.serialbuilder.builder.api.descriptor.nonproxy.NonProxyDescriptorStart;
import marcono1234.serialization.serialbuilder.builder.implementation.ClassTypeNameHelper;

import java.util.function.Function;

public interface DescriptorStart<C> {
    /**
     * Adds a proxy descriptor.
     *
     * @param interfaceNames
     *      name of the interface types implemented by the proxy, in the form returned by {@link Class#getTypeName()},
     *      e.g. {@code java.util.Map$Entry}
     * @return <i>next step</i>
     */
    default C proxyDescriptor(String... interfaceNames) {
        return proxyDescriptor(new Handle(), interfaceNames);
    }

    /**
     * Adds a proxy descriptor and assigns a handle to it.
     *
     * @param unassignedHandle
     *      handle which should be assigned a reference to the written descriptor
     * @param interfaceNames
     *      name of the interface types implemented by the proxy, in the form returned by {@link Class#getTypeName()},
     *      e.g. {@code java.util.Map$Entry}
     * @return <i>next step</i>
     */
    C proxyDescriptor(Handle unassignedHandle, String... interfaceNames);

    /**
     * Adds a proxy descriptor.
     *
     * @param interfaces
     *      interface types implemented by the proxy
     * @return <i>next step</i>
     */
    default C proxyDescriptor(Class<?>... interfaces) {
        return proxyDescriptor(new Handle(), interfaces);
    }

    /**
     * Adds a proxy descriptor and assigns a handle to it.
     *
     * @param unassignedHandle
     *      handle which should be assigned a reference to the written descriptor
     * @param interfaces
     *      interface types implemented by the proxy
     * @return <i>next step</i>
     */
    default C proxyDescriptor(Handle unassignedHandle, Class<?>... interfaces) {
        return proxyDescriptor(unassignedHandle, ClassTypeNameHelper.getInterfaceNames(interfaces));
    }

    /**
     * Begins a non-proxy descriptor.
     *
     * @return <i>next step</i>
     */
    // Note: Only difference in ObjectStreamConstants#PROTOCOL_VERSION_2 is that user can override descriptor writing
    default NonProxyDescriptorStart<C> beginDescriptor() {
        return beginDescriptor(new Handle());
    }

    /**
     * Begins a non-proxy descriptor and assigns a handle to it.
     *
     * @param unassignedHandle
     *      handle which should be assigned a reference to the written descriptor
     * @return <i>next step</i>
     */
    NonProxyDescriptorStart<C> beginDescriptor(Handle unassignedHandle);

    /**
     * Writes a non-proxy descriptor. Allows using a separate method for creating the descriptor
     * without having to interrupt the builder call chain. The writer function must call all builder methods and
     * return the result of the last builder method to make sure the data is written correctly.
     *
     * @return <i>next step</i>
     */
    // Note: Function instead of Consumer is used as parameter type to force user to make all necessary calls
    // and return result of last call
    default C descriptor(Function<NonProxyDescriptorStart<Enclosing>, Enclosing> writer) {
        return descriptor(new Handle(), writer);
    }

    /**
     * Writes a non-proxy descriptor and assigns a handle to it. Allows using a separate method for creating the
     * descriptor without having to interrupt the builder call chain. The writer function must call all builder
     * methods and return the result of the last builder method to make sure the data is written correctly.
     *
     * @param unassignedHandle
     *      handle which should be assigned a reference to the written descriptor
     * @return <i>next step</i>
     */
    @SuppressWarnings("unchecked")
    default C descriptor(Handle unassignedHandle, Function<NonProxyDescriptorStart<Enclosing>, Enclosing> writer) {
        return (C) writer.apply((NonProxyDescriptorStart<Enclosing>) beginDescriptor(unassignedHandle));
    }
}
