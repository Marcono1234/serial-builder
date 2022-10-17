package marcono1234.serialization.serialbuilder.simplebuilder2;

import marcono1234.serialization.serialbuilder.builder.api.ThrowingConsumer;
import marcono1234.serialization.serialbuilder.builder.implementation.ClassTypeNameHelper;
import marcono1234.serialization.serialbuilder.builder.implementation.SerialVersionUidHelper;
import marcono1234.serialization.serialbuilder.simplebuilder2.array.BooleanArray;
import marcono1234.serialization.serialbuilder.simplebuilder2.array.ByteArray;
import marcono1234.serialization.serialbuilder.simplebuilder2.array.CharArray;
import marcono1234.serialization.serialbuilder.simplebuilder2.array.DoubleArray;
import marcono1234.serialization.serialbuilder.simplebuilder2.array.FloatArray;
import marcono1234.serialization.serialbuilder.simplebuilder2.array.IntArray;
import marcono1234.serialization.serialbuilder.simplebuilder2.array.LongArray;
import marcono1234.serialization.serialbuilder.simplebuilder2.array.ObjectArray;
import marcono1234.serialization.serialbuilder.simplebuilder2.array.ShortArray;
import marcono1234.serialization.serialbuilder.simplebuilder2.classdata.ClassData;
import marcono1234.serialization.serialbuilder.simplebuilder2.classdata.ObjectField;
import marcono1234.serialization.serialbuilder.simplebuilder2.classdata.PrimitiveField;
import marcono1234.serialization.serialbuilder.simplebuilder2.classobject.ClassObject;
import marcono1234.serialization.serialbuilder.simplebuilder2.classobject.EnumClass;
import marcono1234.serialization.serialbuilder.simplebuilder2.classobject.ExternalizableClass;
import marcono1234.serialization.serialbuilder.simplebuilder2.classobject.NonSerializableClass;
import marcono1234.serialization.serialbuilder.simplebuilder2.classobject.ProxyClass;
import marcono1234.serialization.serialbuilder.simplebuilder2.classobject.SerializableClass;

import java.io.Externalizable;
import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.util.List;

public class FactoryMethods {
    private FactoryMethods() {}

    public static BooleanArray array(boolean[] array) {
        return new BooleanArray(array);
    }

    public static ByteArray array(byte[] array) {
        return new ByteArray(array);
    }

    public static CharArray array(char[] array) {
        return new CharArray(array);
    }

    public static ShortArray array(short[] array) {
        return new ShortArray(array);
    }

    public static IntArray array(int[] array) {
        return new IntArray(array);
    }

    public static LongArray array(long[] array) {
        return new LongArray(array);
    }

    public static FloatArray array(float[] array) {
        return new FloatArray(array);
    }

    public static DoubleArray array(double[] array) {
        return new DoubleArray(array);
    }

    public static ObjectArray objectArray(String arrayType, List<ObjectBase> elements) {
        return new ObjectArray(arrayType, elements);
    }

    public static ObjectArray objectArray(Class<? extends Object[]> arrayType, List<ObjectBase> elements) {
        return objectArray(arrayType.getTypeName(), elements);
    }

    public static NullObject nullObject() {
        return NullObject.INSTANCE;
    }

    public static StringObject string(String s) {
        return new StringObject(s);
    }

    public static EnumConstant enumConstant(String enumClass, String constantName) {
        return new EnumConstant(enumClass, constantName);
    }

    public static EnumConstant enumConstant(Class<? extends Enum<?>> enumClass, String constantName) {
        return enumConstant(enumClass.getTypeName(), constantName);
    }

    public static <E extends Enum<?>> EnumConstant enumConstant(E enumConstant) {
        return enumConstant(enumConstant.getDeclaringClass(), enumConstant.name());
    }

    public static NonSerializableClass nonSerializableClass(String className) {
        return new NonSerializableClass(className);
    }

    public static ExternalizableClass externalizableClass(String className, long serialVersionUID) {
        return new ExternalizableClass(className, serialVersionUID);
    }

    public static SerializableClass serializableClass(String className, long serialVersionUID) {
        return new SerializableClass(className, serialVersionUID);
    }

    public static ClassObject arrayClass(String className) {
        // For array and record serialVersionUID is ignored, see https://docs.oracle.com/en/java/javase/17/docs/specs/serialization/class.html#stream-unique-identifiers
        return serializableClass(className, 0);
    }

    public static ClassObject recordClass(String className) {
        // For array and record serialVersionUID is ignored, see https://docs.oracle.com/en/java/javase/17/docs/specs/serialization/class.html#stream-unique-identifiers
        return serializableClass(className, 0);
    }

    public static EnumClass enumClass(String className) {
        return new EnumClass(className);
    }

    public static ProxyClass proxyClass(String... interfaceNames) {
        return new ProxyClass(List.of(interfaceNames));
    }

    public static ProxyClass proxyClass(Class<?>... interfaces) {
        return proxyClass(ClassTypeNameHelper.getInterfaceNames(interfaces));
    }

    public static ClassObject classObject(Class<?> c) {
        String className = c.getTypeName();

        if (Proxy.isProxyClass(c)) {
            return proxyClass(c.getInterfaces());
        }
        // ObjectStreamClass also considers Enum.class to be an enum (unlike `Class.isEnum()`)
        else if (c.isEnum() || c == Enum.class) {
            return enumClass(className);
        } else if (c.isArray()) {
            return arrayClass(className);
        } else if (c.isRecord()) {
            return recordClass(className);
        } else if (Externalizable.class.isAssignableFrom(c)) {
            return externalizableClass(className, SerialVersionUidHelper.getSerialVersionUID(c));
        } else if (Serializable.class.isAssignableFrom(c)){
            return serializableClass(className, SerialVersionUidHelper.getSerialVersionUID(c));
        } else {
            return nonSerializableClass(className);
        }
    }

    public static SerializableObject serializableObject(ClassData... classData) {
        return new SerializableObject(List.of(classData));
    }

    public static ExternalizableObject externalizableObject(String typeName, long serialVersionUID, ThrowingConsumer<ObjectDataWriter> writer) {
        return new ExternalizableObject(typeName, serialVersionUID, writer);
    }

    public static ExternalizableObject externalizableObject(Class<? extends  Externalizable> c, ThrowingConsumer<ObjectDataWriter> writer) {
        return externalizableObject(c.getTypeName(), SerialVersionUidHelper.getSerialVersionUID(c), writer);
    }

    public static ProxyObject proxyObject(List<String> interfaceNames, ProxyInvocationHandlerObject invocationHandler) {
        return new ProxyObject(interfaceNames, invocationHandler);
    }

    // TODO: Signature clashes due to erasure
    /*
    public static ProxyObject proxyObject(List<Class<?>> interfaces, ProxyInvocationHandlerObject invocationHandler) {
        return proxyObject(ClassTypeNameHelper.getInterfaceNames(interfaces), invocationHandler);
    }
     */

    /* Serializable object class data factory methods */

    public static ClassData classData(String className, long serialVersionUID, List<SerializedField> fields, ThrowingConsumer<ObjectDataWriter> dataWriter) {
        return new ClassData(className, serialVersionUID, fields, dataWriter);
    }

    public static ClassData classData(String className, long serialVersion, List<SerializedField> fields) {
        return classData(className, serialVersion, fields, null);
    }

    public static ClassData classData(Class<? extends Serializable> c, List<SerializedField> fields, ThrowingConsumer<ObjectDataWriter> dataWriter) {
        return classData(c.getTypeName(), SerialVersionUidHelper.getSerialVersionUID(c), fields, dataWriter);
    }

    public static ClassData classData(Class<? extends Serializable> c, List<SerializedField> fields) {
        return classData(c, fields, null);
    }

    public static PrimitiveField.BooleanField booleanField(String fieldName, boolean value) {
        return new PrimitiveField.BooleanField(fieldName, value);
    }

    public static PrimitiveField.ByteField byteField(String fieldName, byte value) {
        return new PrimitiveField.ByteField(fieldName, value);
    }

    public static PrimitiveField.CharField charField(String fieldName, char value) {
        return new PrimitiveField.CharField(fieldName, value);
    }

    public static PrimitiveField.ShortField shortField(String fieldName, short value) {
        return new PrimitiveField.ShortField(fieldName, value);
    }

    public static PrimitiveField.IntField intField(String fieldName, int value) {
        return new PrimitiveField.IntField(fieldName, value);
    }

    public static PrimitiveField.LongField longField(String fieldName, long value) {
        return new PrimitiveField.LongField(fieldName, value);
    }

    public static PrimitiveField.FloatField floatField(String fieldName, float value) {
        return new PrimitiveField.FloatField(fieldName, value);
    }

    public static PrimitiveField.DoubleField doubleField(String fieldName, double value) {
        return new PrimitiveField.DoubleField(fieldName, value);
    }

    public static ObjectField objectField(String fieldName, String fieldType, ObjectBase object) {
        return new ObjectField(fieldName, fieldType, object);
    }

    public static ObjectField objectField(String fieldName, Class<?> fieldType, ObjectBase object) {
        return objectField(fieldName, ClassTypeNameHelper.getObjectTypeName(fieldType), object);
    }
}
