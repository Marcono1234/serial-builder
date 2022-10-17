package marcono1234.serialization.serialbuilder.simplebuilder2;

import marcono1234.serialization.serialbuilder.simplebuilder2.array.BooleanArray;
import marcono1234.serialization.serialbuilder.simplebuilder2.array.ByteArray;
import marcono1234.serialization.serialbuilder.simplebuilder2.array.CharArray;
import marcono1234.serialization.serialbuilder.simplebuilder2.array.DoubleArray;
import marcono1234.serialization.serialbuilder.simplebuilder2.array.FloatArray;
import marcono1234.serialization.serialbuilder.simplebuilder2.array.IntArray;
import marcono1234.serialization.serialbuilder.simplebuilder2.array.LongArray;
import marcono1234.serialization.serialbuilder.simplebuilder2.array.ObjectArray;
import marcono1234.serialization.serialbuilder.simplebuilder2.array.ShortArray;
import marcono1234.serialization.serialbuilder.simplebuilder2.classobject.ClassObject;

/*
 * TODO:
 *  How to implement this? Should every implementing class store a Handle which it assigns once? But cannot reuse
 *  objects for separate builders then; in the future could maybe implement fluent API on top of this API. Then
 *  Handle of fluent API could internally store the HandleAssignableObject the fluent API created. When ObjectHandle
 *  is then written, the builder could check for reference equality to look up the previously written
 *  HandleAssignableObject.
 */
public sealed interface HandleAssignableObject extends ObjectBase permits EnumConstant, ProxyInvocationHandlerObject, StringObject, BooleanArray, ByteArray, CharArray, DoubleArray, FloatArray, IntArray, LongArray, ObjectArray, ShortArray, ClassObject {
}
