package marcono1234.serialization.serialbuilder.codegen.implementation.streamdata.descriptor;

import marcono1234.serialization.serialbuilder.codegen.implementation.streamdata.StreamObject;

public sealed interface DescriptorStreamObject extends StreamObject permits NonProxyDescriptorData, ProxyDescriptorData {
}
