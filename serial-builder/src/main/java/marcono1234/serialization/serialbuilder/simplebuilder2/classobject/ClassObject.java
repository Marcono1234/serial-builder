package marcono1234.serialization.serialbuilder.simplebuilder2.classobject;

import marcono1234.serialization.serialbuilder.simplebuilder2.HandleAssignableObject;

public sealed interface ClassObject extends HandleAssignableObject permits EnumClass, ExternalizableClass, NonSerializableClass, ProxyClass, SerializableClass {
}
