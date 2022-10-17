package marcono1234.serialization.serialbuilder.simplebuilder2;

// This interface exists to make builder API easier to use by restricting valid invocation
// handler objects; it does not correspond to any serialization format structure because a
// proxy object has regular serializable class data which allows mismatching field values
public sealed interface ProxyInvocationHandlerObject extends HandleAssignableObject permits ExternalizableObject, ObjectHandle, ProxyObject, SerializableObject {
}
