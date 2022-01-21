/**
 * This module contains builder classes for manually creating Java serialization data.
 */
@SuppressWarnings("module") // Suppress warning for module name ending with digits, see also https://bugs.openjdk.java.net/browse/JDK-8264488
module marcono1234.serialization.serialbuilder {
    exports marcono1234.serialization.serialbuilder;

    exports marcono1234.serialization.serialbuilder.builder.api;
    exports marcono1234.serialization.serialbuilder.builder.api.descriptor;
    exports marcono1234.serialization.serialbuilder.builder.api.descriptor.nonproxy;
    exports marcono1234.serialization.serialbuilder.builder.api.object;
    exports marcono1234.serialization.serialbuilder.builder.api.object.array;
    exports marcono1234.serialization.serialbuilder.builder.api.object.externalizable;
    exports marcono1234.serialization.serialbuilder.builder.api.object.serializable;

    exports marcono1234.serialization.serialbuilder.simplebuilder.api;
    exports marcono1234.serialization.serialbuilder.simplebuilder.api.object;
    exports marcono1234.serialization.serialbuilder.simplebuilder.api.object.array;
    exports marcono1234.serialization.serialbuilder.simplebuilder.api.object.proxy;
    exports marcono1234.serialization.serialbuilder.simplebuilder.api.object.serializable;
}
