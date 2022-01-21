package marcono1234.serialization.serialbuilder;

import marcono1234.serialization.serialbuilder.builder.api.Enclosing;
import marcono1234.serialization.serialbuilder.builder.api.Handle;
import marcono1234.serialization.serialbuilder.simplebuilder.api.object.ObjectStart;
import marcono1234.serialization.serialbuilder.simplebuilder.api.object.array.ObjectArrayElements;
import marcono1234.serialization.serialbuilder.simplebuilder.api.object.proxy.ProxyObjectStart;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class SimpleSerialBuilderTest {
    private <T> T deserialize(byte[] data) {
        try (ObjectInputStream objIn = new ObjectInputStream(new ByteArrayInputStream(data))) {
            @SuppressWarnings("unchecked")
            T result = (T) objIn.readObject();
            return result;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static class SerializableClass implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        public int i;
        public int[] array;
        public String s;
    }

    @Test
    void serializable() {
        byte[] actualData = SimpleSerialBuilder.startSerializableObject()
            .beginClassData(SerializableClass.class)
                .primitiveField("i", 6)
                .beginObjectField("array", int[].class)
                    .array(1, 2, 3)
                .endField()
                .beginObjectField("s", String.class)
                    .string("nested-test")
                .endField()
            .endClassData()
        .endObject();

        SerializableClass actualObject = deserialize(actualData);
        assertEquals(6, actualObject.i);
        assertArrayEquals(new int[] {1, 2, 3}, actualObject.array);
        assertEquals("nested-test", actualObject.s);
    }

    private static class ClassWithWriteObject implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        public transient int i;
        public transient String s;
        public transient double d;

        @Serial
        private void writeObject(ObjectOutputStream out) throws IOException {
            out.writeInt(i);
            out.writeObject(s);
            out.writeDouble(d);
        }

        @Serial
        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            i = in.readInt();
            s = (String) in.readObject();
            d = in.readDouble();
        }
    }

    @Test
    void writeObject() {
        byte[] actualData = SimpleSerialBuilder.startSerializableObject()
            .beginClassData(ClassWithWriteObject.class)
                .writeObjectWith(writer -> {
                    writer.writeInt(4);
                    writer.string("test \0 \u0100 \uD800\uDC00 \uDC00");
                    writer.writeDouble(12.2);
                })
            .endClassData()
        .endObject();

        ClassWithWriteObject actualObject = deserialize(actualData);
        assertEquals(4, actualObject.i);
        assertEquals("test \0 \u0100 \uD800\uDC00 \uDC00", actualObject.s);
        assertEquals(12.2, actualObject.d);
    }

    private static class ClassWithNestingWriteObject implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        public transient ClassWithNestingWriteObject nested;

        @Serial
        private void writeObject(ObjectOutputStream out) throws IOException {
            out.writeObject(nested);
        }

        @Serial
        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            nested = (ClassWithNestingWriteObject) in.readObject();
        }
    }

    @Test
    void writeObject_nested() {
        //noinspection CodeBlock2Expr
        byte[] actualData = SimpleSerialBuilder.startSerializableObject()
            .beginClassData(ClassWithNestingWriteObject.class)
                .writeObjectWith(writer -> {
                    writer.beginSerializableObject()
                        .beginClassData(ClassWithNestingWriteObject.class)
                            .writeObjectWith(writer2 -> {
                                //noinspection Convert2MethodRef
                                writer2.nullObject();
                            })
                        .endClassData()
                    .endObject();
                })
            .endClassData()
        .endObject();

        ClassWithNestingWriteObject actualObject = deserialize(actualData);
        ClassWithNestingWriteObject actualNestedObject = actualObject.nested;
        assertNull(actualNestedObject.nested);
    }

    private static class ClassWithWriteObjectWritingFields implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        public int i;
        public String s;

        public transient int i2;
        public transient String s2;
        public transient Object[] array;
        public transient int i3;

        @Serial
        private void writeObject(ObjectOutputStream out) throws IOException {
            out.defaultWriteObject();

            out.writeInt(i2);
            out.writeObject(s2);
            out.writeObject(array);
            out.writeInt(i3);
        }

        @Serial
        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();

            i2 = in.readInt();
            s2 = (String) in.readObject();
            array = (Object[]) in.readObject();
            i3 = in.readInt();
        }
    }

    @Test
    void writeObject_DefaultWrite() {
        byte[] actualData = SimpleSerialBuilder.startSerializableObject()
            .beginClassData(ClassWithWriteObjectWritingFields.class)
                .primitiveField("i", 1)
                .beginObjectField("s", String.class)
                    .string("test")
                .endField()
                .writeObjectWith(writer -> {
                    writer.writeInt(2);
                    writer.string("manually-written");
                    writer.beginObjectArray(Serializable[].class)
                        .beginSerializableObject()
                            .beginClassData(SerializableClass.class)
                                .primitiveField("i", 6)
                                .beginObjectField("array", int[].class)
                                    .array(1, 2, 3)
                                .endField()
                                .beginObjectField("s", String.class)
                                    .string("nested-test")
                                .endField()
                            .endClassData()
                        .endObject()
                    .endArray();
                    writer.writeInt(10);
                })
            .endClassData()
        .endObject();

        ClassWithWriteObjectWritingFields actualObject = deserialize(actualData);
        assertEquals(1, actualObject.i);
        assertEquals("test", actualObject.s);

        assertEquals(2, actualObject.i2);
        assertEquals("manually-written", actualObject.s2);
        assertEquals(10, actualObject.i3);

        Object[] actualArray = actualObject.array;
        assertEquals(1, actualArray.length);

        SerializableClass actualArrayElement = (SerializableClass) actualArray[0];
        assertEquals(6, actualArrayElement.i);
        assertArrayEquals(new int[] {1, 2, 3}, actualArrayElement.array);
        assertEquals("nested-test", actualArrayElement.s);
    }

    private static class ClassWithWriteObjectManuallyWritingFields implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        public transient int i;
        public transient String s;
        public transient int extra;

        @Serial
        private void writeObject(ObjectOutputStream out) throws IOException {
            ObjectOutputStream.PutField putField = out.putFields();
            putField.put("custom-i", i);
            putField.put("custom-s", s);
            out.writeFields();

            out.writeInt(extra);
        }

        @Serial
        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            ObjectInputStream.GetField getField = in.readFields();
            i = getField.get("custom-i", -1);
            s = (String) getField.get("custom-s", null);

            extra = in.readInt();
        }
    }

    @Test
    void writeObject_ManualFieldWrite() {
        byte[] actualData = SimpleSerialBuilder.startSerializableObject()
            .beginClassData(ClassWithWriteObjectManuallyWritingFields.class)
                .primitiveField("custom-i", 2)
                .beginObjectField("custom-s", String.class)
                    .string("test")
                .endField()
                .writeObjectWith(writer -> writer.writeInt(4))
            .endClassData()
        .endObject();

        ClassWithWriteObjectManuallyWritingFields actualObject = deserialize(actualData);
        assertEquals(2, actualObject.i);
        assertEquals("test", actualObject.s);
        assertEquals(4, actualObject.extra);
    }

    private static class ClassWithWriteObjectWritingPrimitiveArray implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        public transient byte[] bytes;

        public ClassWithWriteObjectWritingPrimitiveArray(byte[] bytes) {
            this.bytes = bytes;
        }

        @Serial
        private void writeObject(ObjectOutputStream out) throws IOException {
            out.write(bytes);
        }

        @Serial
        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            bytes = in.readAllBytes();
        }
    }

    /**
     * Tests behavior for large primitive data which would be written in 1024 byte chunks by {@link ObjectOutputStream}.
     */
    @Test
    void largePrimitiveData() {
        final int MAX_BLOCK_SIZE = 1024;
        byte[] primitiveData = new byte[MAX_BLOCK_SIZE + 10];
        // Generate some test data
        for (int i = 0; i < primitiveData.length; i++) {
            primitiveData[i] = (byte) i;
        }

        byte[] actualData = SimpleSerialBuilder.startSerializableObject()
            .beginClassData(ClassWithWriteObjectWritingPrimitiveArray.class)
                .writeObjectWith(writer -> {
                    // Create copy of array to prevent accidental modification
                    writer.write(primitiveData.clone());
                })
            .endClassData()
        .endObject();

        ClassWithWriteObjectWritingPrimitiveArray actualObject = deserialize(actualData);
        assertArrayEquals(primitiveData, actualObject.bytes);
    }

    private static class HierarchyA implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        public String a;
    }

    private static class HierarchyB extends HierarchyA {
        @Serial
        private static final long serialVersionUID = 1L;

        public String b;
    }

    private static class HierarchyC extends HierarchyB {
        @Serial
        private static final long serialVersionUID = 1L;

        public String c;
    }

    /**
     * Serializes data for {@link HierarchyA} and {@link HierarchyC}, but skips data for {@link HierarchyB}.
     */
    @Test
    void serializable_skippedType() {
        byte[] actualData = SimpleSerialBuilder.startSerializableObject()
            .beginClassData(HierarchyA.class)
                .beginObjectField("a", String.class)
                    .string("a")
                .endField()
            .endClassData()
            .beginClassData(HierarchyC.class)
                .beginObjectField("c", String.class)
                    .string("c")
                .endField()
            .endClassData()
        .endObject();

        HierarchyC actualObject = deserialize(actualData);
        assertEquals("a", actualObject.a);
        assertNull(actualObject.b);
        assertEquals("c", actualObject.c);
    }

    private static class ExternalizableClass implements Externalizable {
        @Serial
        private static final long serialVersionUID = 1L;

        public String s;
        public int i;
        public SerializableClass obj;

        public ExternalizableClass() { }

        @Override
        public void writeExternal(ObjectOutput out) {
            fail("Not used by this test");
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            s = (String) in.readObject();
            i = in.readInt();
            obj = (SerializableClass) in.readObject();
        }
    }

    @Test
    void exterializable() {
        byte[] actualData = SimpleSerialBuilder.externalizableObject(ExternalizableClass.class, writer -> {
            writer.string("test");
            writer.writeInt(5);
            writer.beginSerializableObject()
                .beginClassData(SerializableClass.class)
                    .primitiveField("i", 6)
                    .beginObjectField("array", int[].class)
                        .array(1, 2, 3)
                    .endField()
                    .beginObjectField("s", String.class)
                        .string("nested-test")
                    .endField()
                .endClassData()
            .endObject();
        });

        ExternalizableClass actualObject = deserialize(actualData);
        assertEquals("test", actualObject.s);
        assertEquals(5, actualObject.i);

        SerializableClass actualNestedObject = actualObject.obj;
        assertEquals(6, actualNestedObject.i);
        assertArrayEquals(new int[] {1, 2, 3}, actualNestedObject.array);
        assertEquals("nested-test", actualNestedObject.s);
    }

    private static class NestingExternalizableClass implements Externalizable {
        @Serial
        private static final long serialVersionUID = 1L;

        public transient NestingExternalizableClass nested;

        public NestingExternalizableClass() { }

        @Override
        public void writeExternal(ObjectOutput out) {
            fail("Not used by this test");
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            nested = (NestingExternalizableClass) in.readObject();
        }
    }

    @Test
    void exterializable_nested() {
        //noinspection CodeBlock2Expr
        byte[] actualData = SimpleSerialBuilder.externalizableObject(NestingExternalizableClass.class, writer -> {
            writer.externalizableObject(NestingExternalizableClass.class, writer2 -> {
                //noinspection Convert2MethodRef
                writer2.nullObject();
            });
        });

        NestingExternalizableClass actualObject = deserialize(actualData);
        NestingExternalizableClass actualNestedObject = actualObject.nested;
        assertNull(actualNestedObject.nested);
    }

    private enum TestEnum {
        TEST
    }

    private static class ClassWithEnum implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        public TestEnum e;
    }

    @Test
    void enum_() {
        byte[] actualData = SimpleSerialBuilder.startSerializableObject()
            .beginClassData(ClassWithEnum.class)
                .beginObjectField("e", TestEnum.class)
                    .enumConstant(TestEnum.TEST)
                .endField()
            .endClassData()
        .endObject();

        ClassWithEnum actualObject = deserialize(actualData);
        assertSame(TestEnum.TEST, actualObject.e);
    }

    private static class ClassWithClassArray implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        public Class<?>[] classes;
    }

    @Test
    void class_() {
        record TestRecord() { }
        interface TestInterface { }

        byte[] actualData = SimpleSerialBuilder.startSerializableObject()
            .beginClassData(ClassWithClassArray.class)
                .beginObjectField("classes", Class[].class)
                    .beginObjectArray(Class[].class)
                        .class_(SerializableClass.class)
                        .class_(TestEnum.class)
                        .class_(int[].class)
                        .class_(TestRecord.class)
                        .proxyClass(TestInterface.class)
                    .endArray()
                .endField()
            .endClassData()
        .endObject();

        Class<?> expectedProxyClass = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { TestInterface.class }, new SerializableInvocationHandler()).getClass();

        ClassWithClassArray actualObject = deserialize(actualData);
        assertArrayEquals(new Class[] { SerializableClass.class, TestEnum.class, int[].class, TestRecord.class, expectedProxyClass}, actualObject.classes);
    }

    private static class SerializableInvocationHandler implements InvocationHandler, Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        public String result;

        @SuppressWarnings("SuspiciousInvocationHandlerImplementation")
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            return result;
        }
    }

    @Test
    void proxy_serializableInvocationHandler() throws Exception {
        byte[] actualData = SimpleSerialBuilder.startProxyObject(Callable.class)
            .beginSerializableInvocationHandler()
                .beginClassData(SerializableInvocationHandler.class)
                    .beginObjectField("result", String.class)
                        .string("custom-result")
                    .endField()
                .endClassData()
            .endObject()
        .endProxyObject();

        Callable<?> actualObject = deserialize(actualData);
        assertTrue(Proxy.isProxyClass(actualObject.getClass()));
        assertTrue(Proxy.getInvocationHandler(actualObject) instanceof SerializableInvocationHandler);
        assertEquals("custom-result", actualObject.call());
    }

    /**
     * Tests serialized data where {@link InvocationHandler} implementation is itself a proxy implementing
     * {@code InvocationHandler}.
     */
    @Test
    void proxy_proxyInvocationHandler() throws Exception {
        byte[] actualData = SimpleSerialBuilder.startProxyObject(Callable.class)
            .beginProxyInvocationHandler(InvocationHandler.class)
                .beginSerializableInvocationHandler()
                    .beginClassData(SerializableInvocationHandler.class)
                        .beginObjectField("result", String.class)
                            .string("custom-result")
                        .endField()
                    .endClassData()
                .endObject()
            .endProxyObject()
        .endProxyObject();

        Callable<?> actualObject = deserialize(actualData);
        assertTrue(Proxy.isProxyClass(actualObject.getClass()));
        Object actualInvocationHandler = Proxy.getInvocationHandler(actualObject);
        assertTrue(Proxy.isProxyClass(actualInvocationHandler.getClass()));
        assertTrue(Proxy.getInvocationHandler(actualInvocationHandler) instanceof SerializableInvocationHandler);
        assertEquals("custom-result", actualObject.call());
    }

    private static class ExternalizableInvocationHandler implements InvocationHandler, Externalizable {
        @Serial
        private static final long serialVersionUID = 1L;

        public ExternalizableInvocationHandler() { }

        public String result;

        @Override
        public void writeExternal(ObjectOutput out) {
            fail("Not used by this test");
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            result = (String) in.readObject();
        }

        @SuppressWarnings("SuspiciousInvocationHandlerImplementation")
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            return result;
        }
    }

    @Test
    void proxy_externalizableInvocationHandler() throws Exception {
        byte[] actualData = SimpleSerialBuilder.startProxyObject(Callable.class)
            .externalizableInvocationHandler(
                ExternalizableInvocationHandler.class,
                writer -> writer.string("custom-result")
            )
        .endProxyObject();

        Callable<?> actualObject = deserialize(actualData);
        assertTrue(Proxy.isProxyClass(actualObject.getClass()));
        assertEquals("custom-result", actualObject.call());
    }

    private static class ClassWithObjectArray implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        public Object[] array;
    }

    private static class SimpleInvocationHandler implements InvocationHandler, Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @SuppressWarnings("SuspiciousInvocationHandlerImplementation")
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            return null;
        }
    }

    private static Enclosing writeInvocationHandler(ProxyObjectStart<Enclosing> start) {
        return start
            .beginSerializableInvocationHandler()
                .beginClassData(SimpleInvocationHandler.class)
                .endClassData()
            .endObject();
    }

    private static Enclosing writeObjectArrayElements(ObjectArrayElements<Enclosing> start) {
        return start
            .string("test")
            .proxyObject(new Class[]{Runnable.class}, SimpleSerialBuilderTest::writeInvocationHandler)
        .endArray();
    }

    private static Enclosing writeArray(ObjectStart<Enclosing> start) {
        return start.objectArray(Object[].class, SimpleSerialBuilderTest::writeObjectArrayElements);
    }

    @Test
    void separateBuildingMethods() {
        byte[] actualData = SimpleSerialBuilder.startSerializableObject()
            .beginClassData(ClassWithObjectArray.class)
                .objectField("array", Object[].class, SimpleSerialBuilderTest::writeArray)
            .endClassData()
        .endObject();

        ClassWithObjectArray actualObject = deserialize(actualData);
        Object[] actualArray = actualObject.array;
        assertEquals(2, actualArray.length);
        assertEquals("test", actualArray[0]);

        Object actualProxy = actualArray[1];
        assertTrue(Proxy.isProxyClass(actualProxy.getClass()));
        Object actualInvocationHandler = Proxy.getInvocationHandler(actualProxy);
        assertEquals(SimpleInvocationHandler.class, actualInvocationHandler.getClass());
    }

    private static class ClassWithHandles implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        public boolean[] booleans;
        public boolean[] booleansH;

        public byte[] bytes;
        public byte[] bytesH;

        public char[] chars;
        public char[] charsH;

        public short[] shorts;
        public short[] shortsH;

        public int[] ints;
        public int[] intsH;

        public long[] longs;
        public long[] longsH;

        public float[] floats;
        public float[] floatsH;

        public double[] doubles;
        public double[] doublesH;

        public Object[] objects;
        public Object[] objectsH;

        public Serializable serializable;
        public Serializable serializableH;

        public Externalizable externalizable;
        public Externalizable externalizableH;

        public Runnable proxy;
        public Runnable proxyH;
    }

    private static class SimpleSerializable implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
    }

    private static class SimpleExternalizable implements Externalizable {
        @Serial
        private static final long serialVersionUID = 1L;

        public SimpleExternalizable() {
        }

        @Override
        public void writeExternal(ObjectOutput out) {
            fail("Not used by this test");
        }

        @Override
        public void readExternal(ObjectInput in) {
            // Do nothing
        }
    }

    @Test
    void handle() {
        Handle booleansH = new Handle();
        Handle bytesH = new Handle();
        Handle charsH = new Handle();
        Handle shortsH = new Handle();
        Handle intsH = new Handle();
        Handle longsH = new Handle();
        Handle floatsH = new Handle();
        Handle doublesH = new Handle();
        Handle objectsH = new Handle();
        Handle serializableH = new Handle();
        Handle externalizableH = new Handle();
        Handle proxyH = new Handle();

        byte[] actualData = SimpleSerialBuilder.startSerializableObject()
            .beginClassData(ClassWithHandles.class)
                .beginObjectField("booleans", boolean[].class)
                    .array(booleansH, new boolean[] {true})
                .endField()
                .beginObjectField("booleansH", boolean[].class)
                    .objectHandle(booleansH)
                .endField()
                .beginObjectField("bytes", byte[].class)
                    .array(bytesH, new byte[] {1})
                .endField()
                .beginObjectField("bytesH", byte[].class)
                    .objectHandle(bytesH)
                .endField()
                .beginObjectField("chars", char[].class)
                    .array(charsH, new char[] {'a'})
                .endField()
                .beginObjectField("charsH", char[].class)
                    .objectHandle(charsH)
                .endField()
                .beginObjectField("shorts", short[].class)
                    .array(shortsH, new short[] {1})
                .endField()
                .beginObjectField("shortsH", short[].class)
                    .objectHandle(shortsH)
                .endField()
                .beginObjectField("ints", int[].class)
                    .array(intsH, new int[] {1})
                .endField()
                .beginObjectField("intsH", int[].class)
                    .objectHandle(intsH)
                .endField()
                .beginObjectField("longs", long[].class)
                    .array(longsH, new long[] {1})
                .endField()
                .beginObjectField("longsH", long[].class)
                    .objectHandle(longsH)
                .endField()
                .beginObjectField("floats", float[].class)
                    .array(floatsH, new float[] {1f})
                .endField()
                .beginObjectField("floatsH", float[].class)
                    .objectHandle(floatsH)
                .endField()
                .beginObjectField("doubles", double[].class)
                    .array(doublesH, new double[] {1.0})
                .endField()
                .beginObjectField("doublesH", double[].class)
                .   objectHandle(doublesH)
                .endField()
                .beginObjectField("objects", Object[].class)
                    .beginObjectArray(objectsH, String[].class)
                        .string("test")
                    .endArray()
                .endField()
                .beginObjectField("objectsH", Object[].class)
                    .objectHandle(objectsH)
                .endField()
                .beginObjectField("serializable", Serializable.class)
                    .beginSerializableObject(serializableH)
                        .beginClassData(SimpleSerializable.class)
                        .endClassData()
                    .endObject()
                .endField()
                .beginObjectField("serializableH", Serializable.class)
                    .objectHandle(serializableH)
                .endField()
                .beginObjectField("externalizable", Externalizable.class)
                    .externalizableObject(externalizableH, SimpleExternalizable.class, writer -> {
                        // Do nothing
                    })
                .endField()
                .beginObjectField("externalizableH", Externalizable.class)
                    .objectHandle(externalizableH)
                .endField()
                .beginObjectField("proxy", Runnable.class)
                    .beginProxyObject(proxyH, Runnable.class)
                        .beginSerializableInvocationHandler()
                            .beginClassData(SimpleInvocationHandler.class)
                            .endClassData()
                        .endObject()
                    .endProxyObject()
                .endField()
                .beginObjectField("proxyH", Runnable.class)
                    .objectHandle(proxyH)
                .endField()
            .endClassData()
        .endObject();

        ClassWithHandles actualObject = deserialize(actualData);

        assertArrayEquals(new boolean[] {true}, actualObject.booleans);
        assertSame(actualObject.booleans, actualObject.booleansH);

        assertArrayEquals(new byte[] {1}, actualObject.bytes);
        assertSame(actualObject.bytes, actualObject.bytesH);

        assertArrayEquals(new char[] {'a'}, actualObject.chars);
        assertSame(actualObject.chars, actualObject.charsH);

        assertArrayEquals(new short[] {1}, actualObject.shorts);
        assertSame(actualObject.shorts, actualObject.shortsH);

        assertArrayEquals(new int[] {1}, actualObject.ints);
        assertSame(actualObject.ints, actualObject.intsH);

        assertArrayEquals(new long[] {1}, actualObject.longs);
        assertSame(actualObject.longs, actualObject.longsH);

        assertArrayEquals(new float[] {1f}, actualObject.floats);
        assertSame(actualObject.floats, actualObject.floatsH);

        assertArrayEquals(new double[] {1.0}, actualObject.doubles);
        assertSame(actualObject.doubles, actualObject.doublesH);

        assertArrayEquals(new String[] {"test"}, actualObject.objects);
        assertSame(actualObject.objects, actualObject.objectsH);

        assertEquals(SimpleSerializable.class, actualObject.serializable.getClass());
        assertSame(actualObject.serializable, actualObject.serializableH);

        assertEquals(SimpleExternalizable.class, actualObject.externalizable.getClass());
        assertSame(actualObject.externalizable, actualObject.externalizableH);

        assertTrue(Proxy.isProxyClass(actualObject.proxy.getClass()));
        assertEquals(SimpleInvocationHandler.class, Proxy.getInvocationHandler(actualObject.proxy).getClass());
        assertSame(actualObject.proxy, actualObject.proxyH);
    }

    private static class SerializableWithSelfReference implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        public SerializableWithSelfReference self;
    }

    @Test
    void handle_SerializableSelfReference() {
        Handle selfHandle = new Handle();
        byte[] actualData = SimpleSerialBuilder.startSerializableObject(selfHandle)
            .beginClassData(SerializableWithSelfReference.class)
                .beginObjectField("self", SerializableWithSelfReference.class)
                    .objectHandle(selfHandle)
                .endField()
            .endClassData()
        .endObject();

        SerializableWithSelfReference actualObject = deserialize(actualData);
        assertSame(actualObject, actualObject.self);
    }

    private static class ExternalizableWithSelfReference implements Externalizable {
        @Serial
        private static final long serialVersionUID = 1L;

        public ExternalizableWithSelfReference self;

        public ExternalizableWithSelfReference() {
        }

        @Override
        public void writeExternal(ObjectOutput out) {
            fail("Not used by this test");
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            self = (ExternalizableWithSelfReference) in.readObject();
        }
    }

    @Test
    void handle_ExternalizableSelfReference() {
        Handle selfHandle = new Handle();
        byte[] actualData = SimpleSerialBuilder.externalizableObject(
            selfHandle,
            ExternalizableWithSelfReference.class,
            writer -> writer.objectHandle(selfHandle)
        );

        ExternalizableWithSelfReference actualObject = deserialize(actualData);
        assertSame(actualObject, actualObject.self);
    }

    /**
     * Test case where proxy is its own invocation handler.
     */
    @Test
    void handle_ProxySelfReference() {
        Handle selfHandle = new Handle();
        byte[] actualData = SimpleSerialBuilder.startProxyObject(selfHandle, InvocationHandler.class)
            .invocationHandlerHandle(selfHandle)
        .endProxyObject();

        Object actualObject = deserialize(actualData);
        assertTrue(Proxy.isProxyClass(actualObject.getClass()));
        Object actualInvocationHandler = Proxy.getInvocationHandler(actualObject);
        assertSame(actualObject, actualInvocationHandler);
    }
}
