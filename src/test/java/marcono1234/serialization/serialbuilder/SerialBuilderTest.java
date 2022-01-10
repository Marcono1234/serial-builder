package marcono1234.serialization.serialbuilder;

import marcono1234.serialization.serialbuilder.builder.api.Enclosing;
import marcono1234.serialization.serialbuilder.builder.api.Handle;
import marcono1234.serialization.serialbuilder.builder.api.ObjectBuildingDataOutput;
import marcono1234.serialization.serialbuilder.builder.api.descriptor.DescriptorsList;
import marcono1234.serialization.serialbuilder.builder.api.descriptor.nonproxy.NonProxyDescriptorStart;
import marcono1234.serialization.serialbuilder.builder.api.object.serializable.SlotStart;
import marcono1234.serialization.serialbuilder.builder.api.object.serializable.SlotsStart;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static java.io.ObjectStreamConstants.SC_BLOCK_DATA;
import static java.io.ObjectStreamConstants.SC_ENUM;
import static java.io.ObjectStreamConstants.SC_EXTERNALIZABLE;
import static java.io.ObjectStreamConstants.SC_SERIALIZABLE;
import static java.io.ObjectStreamConstants.SC_WRITE_METHOD;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

class SerialBuilderTest {
    private static byte[] serialize(Serializable obj) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ObjectOutputStream objOut = new ObjectOutputStream(out)) {
            objOut.writeObject(obj);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return out.toByteArray();
    }

    private static class SimpleSerializableClass implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        public int i;

        public SimpleSerializableClass(int i) {
            this.i = i;
        }
    }

    @Test
    void simple() {
        byte[] actualData = SerialBuilder.startSerializableObject()
            .beginDescriptorHierarchy()
                .beginDescriptor()
                    .type(SimpleSerializableClass.class)
                    .uid(SimpleSerializableClass.serialVersionUID)
                    .flags(SC_SERIALIZABLE)
                    .beginPrimitiveFieldDescriptors()
                        .intField("i")
                    .endPrimitiveFieldDescriptors()
                .endDescriptor()
            .endDescriptorHierarchy()
            .beginSlots()
                .beginSlot()
                    .beginPrimitiveFields()
                        .value(1)
                    .endPrimitiveFields()
                .endSlot()
            .endSlots()
        .endObject();

        byte[] expectedData = serialize(new SimpleSerializableClass(1));
        assertArrayEquals(expectedData, actualData);
    }

    private static class ClassWithNested implements Serializable {
        private static class NestedSerializable implements Serializable {
            @Serial
            private static final long serialVersionUID = 1L;

            public int n;

            public NestedSerializable(int n) {
                this.n = n;
            }
        }

        private static class NestedExternalizable implements Externalizable {
            @Serial
            private static final long serialVersionUID = 1L;

            public NestedExternalizable() {
            }

            @Override
            public void writeExternal(ObjectOutput out) throws IOException {
                out.writeInt(5);
                out.writeBoolean(true);
            }

            @Override
            public void readExternal(ObjectInput in) {
                fail("Reading is not supported");
            }
        }

        @Serial
        private static final long serialVersionUID = 1L;

        public NestedSerializable serializable;
        public NestedExternalizable externalizable;
        public int[] ints;

        public ClassWithNested(NestedSerializable serializable, NestedExternalizable externalizable, int[] ints) {
            this.serializable = serializable;
            this.externalizable = externalizable;
            this.ints = ints;
        }
    }

    private static Enclosing writeDescriptor(NonProxyDescriptorStart<Enclosing> start) {
        return start
            .type(ClassWithNested.class)
            .uid(ClassWithNested.serialVersionUID)
            .flags(SC_SERIALIZABLE)
            .beginObjectFieldDescriptors()
                // Note: Fields are sorted, see java.io.ObjectStreamField.compareTo
                .objectField("externalizable", ClassWithNested.NestedExternalizable.class)
                .objectField("ints", int[].class)
                .objectField("serializable", ClassWithNested.NestedSerializable.class)
            .endObjectFieldDescriptors()
        .endDescriptor();
    }

    private static Enclosing writeDescriptorHierarchy(DescriptorsList<Enclosing> start) {
        return start.
            descriptor(SerialBuilderTest::writeDescriptor)
        .endDescriptorHierarchy();
    }

    private static SlotsStart<Enclosing> writeSlot(SlotStart<Enclosing> start) {
        return start
            .beginObjectFields()
                .externalizableObject(writer -> writer
                        .beginDescriptorHierarchy()
                            .beginDescriptor()
                                .type(ClassWithNested.NestedExternalizable.class)
                                .uid(ClassWithNested.NestedExternalizable.serialVersionUID)
                                .flags(SC_EXTERNALIZABLE | SC_BLOCK_DATA)
                            .endDescriptor()
                        .endDescriptorHierarchy()
                        .writeExternalWith(dataOutput -> {
                            dataOutput.writeInt(5);
                            dataOutput.writeBoolean(true);
                        })
                    .endObject()
                )
                .array(writer -> writer
                        .beginDescriptorHierarchy()
                            .beginDescriptor()
                                .typeWithUid(int[].class)
                                .flags(SC_SERIALIZABLE)
                            .endDescriptor()
                        .endDescriptorHierarchy()
                        .elements(new int[] {1, 2})
                    .endArray()
                )
                .serializableObject(writer -> writer
                        .beginDescriptorHierarchy()
                            .beginDescriptor()
                                .type(ClassWithNested.NestedSerializable.class)
                                .uid(ClassWithNested.NestedSerializable.serialVersionUID)
                                .flags(SC_SERIALIZABLE)
                                .beginPrimitiveFieldDescriptors()
                                    .intField("n")
                                .endPrimitiveFieldDescriptors()
                            .endDescriptor()
                        .endDescriptorHierarchy()
                        .beginSlots()
                            .beginSlot()
                                .beginPrimitiveFields()
                                    .value(3)
                                .endPrimitiveFields()
                            .endSlot()
                        .endSlots()
                    .endObject()
                )
            .endObjectFields()
        .endSlot();
    }

    @Test
    void separateBuildingMethods() {
        byte[] actualData = SerialBuilder.startSerializableObject()
            .descriptorHierarchy(SerialBuilderTest::writeDescriptorHierarchy)
            .beginSlots()
                .slot(SerialBuilderTest::writeSlot)
            .endSlots()
        .endObject();

        byte[] expectedData = serialize(new ClassWithNested(new ClassWithNested.NestedSerializable(3), new ClassWithNested.NestedExternalizable(), new int[] {1, 2}));
        assertArrayEquals(expectedData, actualData);
    }

    private static class SerializableClass implements Serializable {
        private static class NestedClass implements Serializable {
            @Serial
            private static final long serialVersionUID = 1L;

            public long l;

            public NestedClass(long l) {
                this.l = l;
            }
        }

        @Serial
        private static final long serialVersionUID = 1L;

        public int i;
        public float f;

        public Class<?> c;
        public String s;
        public NestedClass nested;
        public SimpleSerializableClass other;

        public SerializableClass(int i, float f, Class<?> c, String s, NestedClass nested) {
            this.i = i;
            this.f = f;
            this.c = c;
            this.s = s;
            this.nested = nested;
            this.other = null;
        }
    }

    @Test
    void nested() {
        byte[] actualData = SerialBuilder.startSerializableObject()
            .beginDescriptorHierarchy()
                .beginDescriptor()
                    .type(SerializableClass.class)
                    .uid(SerializableClass.serialVersionUID)
                    .flags(SC_SERIALIZABLE)
                    .beginPrimitiveFieldDescriptors()
                        // Note: Fields are sorted, see java.io.ObjectStreamField.compareTo
                        .floatField("f")
                        .intField("i")
                    .endPrimitiveFieldDescriptors()
                    .beginObjectFieldDescriptors()
                        .objectField("c", Class.class)
                        .objectField("nested", SerializableClass.NestedClass.class)
                        .objectField("other", SimpleSerializableClass.class)
                        .objectField("s", String.class)
                    .endObjectFieldDescriptors()
                .endDescriptor()
            .endDescriptorHierarchy()
            .beginSlots()
                .beginSlot()
                    .beginPrimitiveFields()
                        .value(10f)
                        .value(1)
                    .endPrimitiveFields()
                    .beginObjectFields()
                        .beginClass()
                            .beginDescriptorHierarchy()
                                .beginDescriptor()
                                    .typeWithUid(Serializable.class)
                                    .flags(SC_SERIALIZABLE)
                                .endDescriptor()
                            .endDescriptorHierarchy()
                        .endClass()
                        .beginSerializableObject()
                            .beginDescriptorHierarchy()
                                .beginDescriptor()
                                    .type(SerializableClass.NestedClass.class)
                                    .uid(SerializableClass.NestedClass.serialVersionUID)
                                    .flags(SC_SERIALIZABLE)
                                    .beginPrimitiveFieldDescriptors()
                                        .longField("l")
                                    .endPrimitiveFieldDescriptors()
                                .endDescriptor()
                            .endDescriptorHierarchy()
                            .beginSlots()
                                .beginSlot()
                                    .beginPrimitiveFields()
                                        .value(5L)
                                    .endPrimitiveFields()
                                .endSlot()
                            .endSlots()
                        .endObject()
                        .nullObject()
                        .string("test")
                    .endObjectFields()
                .endSlot()
            .endSlots()
        .endObject();

        byte[] expectedData = serialize(new SerializableClass(1, 10f, Serializable.class, "test", new SerializableClass.NestedClass(5)));
        assertArrayEquals(expectedData, actualData);
    }

    private static class BaseClass implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        public int i;

        public BaseClass(int i) {
            this.i = i;
        }
    }

    private static class SubClass extends BaseClass {
        @Serial
        private static final long serialVersionUID = 1L;

        public char c;

        public SubClass(int i, char c) {
            super(i);
            this.c = c;
        }
    }

    @Test
    void hierarchy() {
        byte[] actualData = SerialBuilder.startSerializableObject()
            .beginDescriptorHierarchy()
                .beginDescriptor()
                    .type(SubClass.class)
                    .uid(SubClass.serialVersionUID)
                    .flags(SC_SERIALIZABLE)
                    .beginPrimitiveFieldDescriptors()
                        .charField("c")
                    .endPrimitiveFieldDescriptors()
                .endDescriptor()
                .beginDescriptor()
                    .type(BaseClass.class)
                    .uid(BaseClass.serialVersionUID)
                    .flags(SC_SERIALIZABLE)
                    .beginPrimitiveFieldDescriptors()
                        .intField("i")
                    .endPrimitiveFieldDescriptors()
                .endDescriptor()
            .endDescriptorHierarchy()
            .beginSlots()
                // Slots are in reverse order (subclass -> superclass), see java.io.ObjectStreamClass.getClassDataLayout0
                .beginSlot()
                    .beginPrimitiveFields()
                        .value(5)
                    .endPrimitiveFields()
                .endSlot()
                .beginSlot()
                    .beginPrimitiveFields()
                        .value('a')
                    .endPrimitiveFields()
                .endSlot()
            .endSlots()
        .endObject();

        byte[] expectedData = serialize(new SubClass(5, 'a'));
        assertArrayEquals(expectedData, actualData);
    }

    private enum TestEnum {
        TEST
    }

    private static class ClassWithEnum implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        public TestEnum e;

        public ClassWithEnum(TestEnum e) {
            this.e = e;
        }
    }

    @Test
    void enum_() {
        byte[] actualData = SerialBuilder.startSerializableObject()
            .beginDescriptorHierarchy()
                .beginDescriptor()
                    .type(ClassWithEnum.class)
                    .uid(ClassWithEnum.serialVersionUID)
                    .flags(SC_SERIALIZABLE)
                    .beginObjectFieldDescriptors()
                        .objectField("e", TestEnum.class)
                    .endObjectFieldDescriptors()
                .endDescriptor()
            .endDescriptorHierarchy()
            .beginSlots()
                .beginSlot()
                    .beginObjectFields()
                        .beginEnum()
                            .beginDescriptorHierarchy()
                                .beginDescriptor()
                                    .enumClass(TestEnum.class)
                                    .flags(SC_SERIALIZABLE | SC_ENUM)
                                .endDescriptor()
                                .beginDescriptor()
                                    .enumClass(Enum.class)
                                    .flags(SC_SERIALIZABLE | SC_ENUM)
                                .endDescriptor()
                            .endDescriptorHierarchy()
                            .name("TEST")
                        .endEnum()
                    .endObjectFields()
                .endSlot()
            .endSlots()
        .endObject();

        byte[] expectedData = serialize(new ClassWithEnum(TestEnum.TEST));
        assertArrayEquals(expectedData, actualData);
    }

    private static class ClassWithArray implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        public int[] ints;
        public Object[] objects;

        public ClassWithArray(int[] ints, Object[] objects) {
            this.ints = ints;
            this.objects = objects;
        }
    }

    @Test
    void array() {
        byte[] actualData = SerialBuilder.startSerializableObject()
            .beginDescriptorHierarchy()
                .beginDescriptor()
                    .type(ClassWithArray.class)
                    .uid(ClassWithArray.serialVersionUID)
                    .flags(SC_SERIALIZABLE)
                    .beginObjectFieldDescriptors()
                        .objectField("ints", int[].class)
                        .objectField("objects", Object[].class)
                    .endObjectFieldDescriptors()
                .endDescriptor()
            .endDescriptorHierarchy()
            .beginSlots()
                .beginSlot()
                    .beginObjectFields()
                        .beginArray()
                            .beginDescriptorHierarchy()
                                .beginDescriptor()
                                    .type(int[].class)
                                    .uid(5600894804908749477L) // Generated int[] serialVersionUID
                                    .flags(SC_SERIALIZABLE)
                                .endDescriptor()
                            .endDescriptorHierarchy()
                            .elements(new int[] {1, 2, 3})
                        .endArray()
                        .beginArray()
                            .beginDescriptorHierarchy()
                                .beginDescriptor()
                                    .typeWithUid(Serializable[].class)
                                    .flags(SC_SERIALIZABLE)
                                .endDescriptor()
                            .endDescriptorHierarchy()
                            .beginObjectElements()
                                .string("test \0 \u0100 \uD800\uDC00 \uDC00")
                                .beginSerializableObject()
                                    .beginDescriptorHierarchy()
                                        .beginDescriptor()
                                            .type(SimpleSerializableClass.class)
                                            .uid(SimpleSerializableClass.serialVersionUID)
                                            .flags(SC_SERIALIZABLE)
                                            .beginPrimitiveFieldDescriptors()
                                                .intField("i")
                                            .endPrimitiveFieldDescriptors()
                                        .endDescriptor()
                                    .endDescriptorHierarchy()
                                    .beginSlots()
                                        .beginSlot()
                                            .beginPrimitiveFields()
                                                .value(1)
                                            .endPrimitiveFields()
                                        .endSlot()
                                    .endSlots()
                                .endObject()
                            .endElements()
                        .endArray()
                    .endObjectFields()
                .endSlot()
            .endSlots()
        .endObject();

        byte[] expectedData = serialize(new ClassWithArray(new int[] {1, 2, 3}, new Serializable[] {"test \0 \u0100 \uD800\uDC00 \uDC00", new SimpleSerializableClass(1)}));
        assertArrayEquals(expectedData, actualData);
    }

    private static class ExternalizableClass implements Externalizable {
        @Serial
        private static final long serialVersionUID = 1L;

        @SuppressWarnings("unused")
        public int i;

        public ExternalizableClass() {
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeInt(5);
            out.writeBoolean(true);

            out.writeObject("test");
            out.writeObject(new Serializable[] {new SimpleSerializableClass(1)});
        }

        @Override
        public void readExternal(ObjectInput in) {
            fail("Reading is not supported");
        }
    }

    private static void writeArrayToObjectOutput(ObjectBuildingDataOutput output) {
        output.beginArray()
            .beginDescriptorHierarchy()
                .beginDescriptor()
                    .typeWithUid(Serializable[].class)
                    .flags(SC_SERIALIZABLE)
                .endDescriptor()
            .endDescriptorHierarchy()
            .beginObjectElements()
                .beginSerializableObject()
                    .beginDescriptorHierarchy()
                        .beginDescriptor()
                            .type(SimpleSerializableClass.class)
                            .uid(SimpleSerializableClass.serialVersionUID)
                            .flags(SC_SERIALIZABLE)
                            .beginPrimitiveFieldDescriptors()
                                .intField("i")
                            .endPrimitiveFieldDescriptors()
                        .endDescriptor()
                    .endDescriptorHierarchy()
                    .beginSlots()
                        .beginSlot()
                            .beginPrimitiveFields()
                                .value(1)
                            .endPrimitiveFields()
                        .endSlot()
                    .endSlots()
                .endObject()
            .endElements()
        .endArray();
    }

    @Test
    void externalizable() {
        byte[] actualData = SerialBuilder.startExternalizableObject()
            .beginDescriptorHierarchy()
                .beginDescriptor()
                    .type(ExternalizableClass.class)
                    .uid(ExternalizableClass.serialVersionUID)
                    .flags(SC_EXTERNALIZABLE | SC_BLOCK_DATA)
                .endDescriptor()
            .endDescriptorHierarchy()
            .writeExternalWith(writer -> {
                writer.writeInt(5);
                writer.writeBoolean(true);

                writer.string("test");
                writeArrayToObjectOutput(writer);
            })
        .endObject();

        byte[] expectedData = serialize(new ExternalizableClass());
        assertArrayEquals(expectedData, actualData);
    }

    private static class ClassWithExternalizable implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        public ExternalizableClass e;

        public ClassWithExternalizable(ExternalizableClass e) {
            this.e = e;
        }
    }

    @Test
    void nestedExternalizable() {
        byte[] actualData = SerialBuilder.startSerializableObject()
            .beginDescriptorHierarchy()
                .beginDescriptor()
                    .type(ClassWithExternalizable.class)
                    .uid(ClassWithExternalizable.serialVersionUID)
                    .flags(SC_SERIALIZABLE)
                    .beginObjectFieldDescriptors()
                        .objectField("e", ExternalizableClass.class)
                    .endObjectFieldDescriptors()
                .endDescriptor()
            .endDescriptorHierarchy()
            .beginSlots()
                .beginSlot()
                    .beginObjectFields()
                        .beginExternalizableObject()
                            .beginDescriptorHierarchy()
                                .beginDescriptor()
                                    .type(ExternalizableClass.class)
                                    .uid(ExternalizableClass.serialVersionUID)
                                    .flags(SC_EXTERNALIZABLE | SC_BLOCK_DATA)
                                .endDescriptor()
                            .endDescriptorHierarchy()
                        .writeExternalWith(writer -> {
                            writer.writeInt(5);
                            writer.writeBoolean(true);

                            writer.string("test");
                            writeArrayToObjectOutput(writer);
                        })
                        .endObject()
                    .endObjectFields()
                .endSlot()
            .endSlots()
        .endObject();

        byte[] expectedData = serialize(new ClassWithExternalizable(new ExternalizableClass()));
        assertArrayEquals(expectedData, actualData);
    }

    private static class ClassWithWriteObject implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Serial
        private void writeObject(ObjectOutputStream out) throws IOException {
            out.writeInt(5);
            out.writeBoolean(true);

            out.writeObject("test");
            out.writeObject(new Serializable[] {new SimpleSerializableClass(1)});
        }
    }

    @Test
    void writeObject() {
        byte[] actualData = SerialBuilder.startSerializableObject()
            .beginDescriptorHierarchy()
                .beginDescriptor()
                    .type(ClassWithWriteObject.class)
                    .uid(ClassWithWriteObject.serialVersionUID)
                    .flags(SC_SERIALIZABLE | SC_WRITE_METHOD)
                .endDescriptor()
            .endDescriptorHierarchy()
            .beginSlots()
                .beginSlot()
                    .writeObjectWith(writer -> {
                        writer.writeInt(5);
                        writer.writeBoolean(true);

                        writer.string("test");
                        writeArrayToObjectOutput(writer);
                    })
                .endSlot()
            .endSlots()
        .endObject();

        byte[] expectedData = serialize(new ClassWithWriteObject());
        assertArrayEquals(expectedData, actualData);
    }

    private static class ClassWithWriteObjectWritingFields implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        public int i2;
        public String s;

        public ClassWithWriteObjectWritingFields(int i2, String s) {
            this.i2 = i2;
            this.s = s;
        }

        @Serial
        private void writeObject(ObjectOutputStream out) throws IOException {
            out.defaultWriteObject();
            out.writeInt(5);
            out.writeBoolean(true);

            out.writeObject("test2");
            out.writeObject(new Serializable[] {new SimpleSerializableClass(1)});
        }
    }

    @Test
    void writeObject_DefaultWrite() {
        byte[] actualData = SerialBuilder.startSerializableObject()
            .beginDescriptorHierarchy()
                .beginDescriptor()
                    .type(ClassWithWriteObjectWritingFields.class)
                    .uid(ClassWithWriteObjectWritingFields.serialVersionUID)
                    .flags(SC_SERIALIZABLE | SC_WRITE_METHOD)
                    .beginPrimitiveFieldDescriptors()
                        .intField("i2")
                    .endPrimitiveFieldDescriptors()
                    .beginObjectFieldDescriptors()
                        .objectField("s", String.class)
                    .endObjectFieldDescriptors()
                .endDescriptor()
            .endDescriptorHierarchy()
            .beginSlots()
                .beginSlot()
                    .beginPrimitiveFields()
                        .value(3)
                    .endPrimitiveFields()
                    .beginObjectFields()
                        .string("test")
                    .endObjectFields()
                    .writeObjectWith(writer -> {
                        writer.writeInt(5);
                        writer.writeBoolean(true);

                        writer.string("test2");
                        writeArrayToObjectOutput(writer);
                    })
                .endSlot()
            .endSlots()
        .endObject();

        byte[] expectedData = serialize(new ClassWithWriteObjectWritingFields(3, "test"));
        assertArrayEquals(expectedData, actualData);
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
    }

    /**
     * Tests behavior for large primitive data which would be written in 1024 byte chunks by {@link ObjectOutputStream}.
     */
    @Disabled("Own block data stream implementation currently does not support 1024 byte chunks (though ObjectInputStream works even when data is not in 1024 byte chunks)")
    @Test
    void largePrimitiveData() {
        final int MAX_BLOCK_SIZE = 1024;
        byte[] primitiveData = new byte[MAX_BLOCK_SIZE + 10];
        // Generate some test data
        for (int i = 0; i < primitiveData.length; i++) {
            primitiveData[i] = (byte) i;
        }

        byte[] actualData = SerialBuilder.startSerializableObject()
            .beginDescriptorHierarchy()
                .beginDescriptor()
                    .typeWithUid(ClassWithWriteObjectWritingPrimitiveArray.class)
                    .flags(SC_SERIALIZABLE | SC_WRITE_METHOD)
                .endDescriptor()
            .endDescriptorHierarchy()
            .beginSlots()
                .beginSlot()
                    .writeObjectWith(writer -> {
                        // Create copy of array to prevent accidental modification
                        writer.write(primitiveData.clone());
                    })
                .endSlot()
            .endSlots()
        .endObject();

        byte[] expectedData = serialize(new ClassWithWriteObjectWritingPrimitiveArray(primitiveData));
        assertArrayEquals(expectedData, actualData);
    }

    private static class SerializableInvocationHandler implements InvocationHandler, Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @SuppressWarnings("SuspiciousInvocationHandlerImplementation")
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            return null;
        }
    }

    @Test
    void proxy() {
        interface InterfaceA {}
        interface InterfaceB {}

        Object proxyObject = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] {InterfaceA.class, InterfaceB.class}, new SerializableInvocationHandler());

        byte[] actualData = SerialBuilder.startSerializableObject()
            .beginDescriptorHierarchy()
                .proxyDescriptor(InterfaceA.class, InterfaceB.class)
                .beginDescriptor()
                    .typeWithUid(Proxy.class)
                    .flags(SC_SERIALIZABLE)
                    .beginObjectFieldDescriptors()
                        .objectField("h", InvocationHandler.class)
                    .endObjectFieldDescriptors()
                .endDescriptor()
            .endDescriptorHierarchy()
            .beginSlots()
                .beginSlot()
                    .beginObjectFields()
                        .beginSerializableObject()
                            .beginDescriptorHierarchy()
                                .beginDescriptor()
                                    .type(SerializableInvocationHandler.class)
                                    .uid(SerializableInvocationHandler.serialVersionUID)
                                    .flags(SC_SERIALIZABLE)
                                .endDescriptor()
                            .endDescriptorHierarchy()
                        .endObject()
                    .endObjectFields()
                .endSlot()
                .beginSlot()
                .endSlot()
            .endSlots()
        .endObject();

        byte[] expectedData = serialize((Serializable) proxyObject);
        assertArrayEquals(expectedData, actualData);
    }

    private static class ClassWithHandles implements Serializable {
        @SuppressWarnings({"ExternalizableWithoutPublicNoArgConstructor", "ClassCanBeRecord"})
        private static class NestedExternalizable implements Externalizable {
            @Serial
            private static final long serialVersionUID = 1L;

            private final String s;

            public NestedExternalizable(String s) {
                this.s = s;
            }

            @Override
            public void writeExternal(ObjectOutput out) throws IOException {
                out.writeInt(5);
                out.writeBoolean(true);
                out.writeObject(s);
            }

            @Override
            public void readExternal(ObjectInput in) {
                fail("Reading is not supported");
            }
        }

        @Serial
        private static final long serialVersionUID = 1L;

        public Object[] objects;

        public ClassWithHandles() {
            int[] array = new int[0];
            Class<?> class_ = Serializable.class;
            TestEnum enum_ = TestEnum.TEST;
            String string = "test-string";
            Serializable serializable = new SimpleSerializableClass(1);
            Externalizable externalizable = new NestedExternalizable(string);
            // Both proxy instances will share the same proxy class
            Object proxy1 = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] {Serializable.class}, new SerializableInvocationHandler());
            Object proxy2 = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] {Serializable.class}, new SerializableInvocationHandler());
            assertSame(proxy1.getClass(), proxy2.getClass());

            objects = new Object[] {
                array,
                array,
                class_,
                class_,
                enum_,
                enum_,
                string,
                string,
                serializable,
                serializable,
                externalizable,
                externalizable,
                proxy1,
                proxy2,
            };
        }
    }

    @Test
    void handle() {
        Handle arrayHandle = new Handle();
        Handle classHandle = new Handle();
        Handle enumHandle = new Handle();
        Handle stringHandle = new Handle();
        Handle serializableHandle = new Handle();
        Handle externalizableHandle = new Handle();
        Handle descHandle = new Handle();
        Handle proxyDescHandle = new Handle();

        byte[] actualData = SerialBuilder.startSerializableObject()
            .beginDescriptorHierarchy()
                .beginDescriptor()
                    .type(ClassWithHandles.class)
                    .uid(ClassWithHandles.serialVersionUID)
                    .flags(SC_SERIALIZABLE)
                    .beginObjectFieldDescriptors()
                        .objectField("objects", Object[].class)
                    .endObjectFieldDescriptors()
                .endDescriptor()
            .endDescriptorHierarchy()
            .beginSlots()
                .beginSlot()
                    .beginObjectFields()
                        .beginArray()
                            .beginDescriptorHierarchy()
                                .beginDescriptor()
                                    .typeWithUid(Object[].class)
                                    .flags(SC_SERIALIZABLE)
                                .endDescriptor()
                            .endDescriptorHierarchy()
                            .beginObjectElements()
                                // Array
                                .beginArray(arrayHandle)
                                    .beginDescriptorHierarchy()
                                        .beginDescriptor()
                                            .typeWithUid(int[].class)
                                            .flags(SC_SERIALIZABLE)
                                        .endDescriptor()
                                    .endDescriptorHierarchy()
                                    .elements(new int[0])
                                .endArray()
                                .objectHandle(arrayHandle)
                                // Class
                                .beginClass(classHandle)
                                    .beginDescriptorHierarchy()
                                        .beginDescriptor()
                                            .typeWithUid(Serializable.class)
                                            .flags(SC_SERIALIZABLE)
                                        .endDescriptor()
                                    .endDescriptorHierarchy()
                                .endClass()
                                .objectHandle(classHandle)
                                // Enum
                                .beginEnum(enumHandle)
                                    .beginDescriptorHierarchy()
                                        .beginDescriptor()
                                            .enumClass(TestEnum.class)
                                            .flags(SC_SERIALIZABLE | SC_ENUM)
                                        .endDescriptor()
                                        .beginDescriptor()
                                            .enumClass(Enum.class)
                                            .flags(SC_SERIALIZABLE | SC_ENUM)
                                        .endDescriptor()
                                    .endDescriptorHierarchy()
                                    .name("TEST")
                                .endEnum()
                                .objectHandle(enumHandle)
                                // String
                                .string(stringHandle, "test-string")
                                .objectHandle(stringHandle)
                                // Serializable
                                .beginSerializableObject(serializableHandle)
                                    .beginDescriptorHierarchy()
                                        .beginDescriptor()
                                            .type(SimpleSerializableClass.class)
                                            .uid(SimpleSerializableClass.serialVersionUID)
                                            .flags(SC_SERIALIZABLE)
                                            .beginPrimitiveFieldDescriptors()
                                                .intField("i")
                                            .endPrimitiveFieldDescriptors()
                                        .endDescriptor()
                                    .endDescriptorHierarchy()
                                    .beginSlots()
                                        .beginSlot()
                                            .beginPrimitiveFields()
                                                .value(1)
                                            .endPrimitiveFields()
                                        .endSlot()
                                    .endSlots()
                                .endObject()
                                .objectHandle(serializableHandle)
                                // Externalizable
                                .beginExternalizableObject(externalizableHandle)
                                    .beginDescriptorHierarchy()
                                        .beginDescriptor()
                                            .type(ClassWithHandles.NestedExternalizable.class)
                                            .uid(ClassWithHandles.NestedExternalizable.serialVersionUID)
                                            .flags(SC_EXTERNALIZABLE | SC_BLOCK_DATA)
                                        .endDescriptor()
                                    .endDescriptorHierarchy()
                                    .writeExternalWith(writer -> {
                                        writer.writeInt(5);
                                        writer.writeBoolean(true);
                                        writer.objectHandle(stringHandle);
                                    })
                                .endObject()
                                .objectHandle(externalizableHandle)
                                // Proxy and non-proxy descriptor
                                .beginSerializableObject()
                                    .beginDescriptorHierarchy()
                                        .proxyDescriptor(proxyDescHandle, Serializable.class)
                                        .beginDescriptor()
                                            .typeWithUid(Proxy.class)
                                            .flags(SC_SERIALIZABLE)
                                            .beginObjectFieldDescriptors()
                                                .objectField("h", InvocationHandler.class)
                                            .endObjectFieldDescriptors()
                                        .endDescriptor()
                                    .endDescriptorHierarchy()
                                    .beginSlots()
                                        .beginSlot()
                                            .beginObjectFields()
                                                .beginSerializableObject()
                                                    .beginDescriptorHierarchy()
                                                        .beginDescriptor(descHandle)
                                                            .type(SerializableInvocationHandler.class)
                                                            .uid(SerializableInvocationHandler.serialVersionUID)
                                                            .flags(SC_SERIALIZABLE)
                                                        .endDescriptor()
                                                    .endDescriptorHierarchy()
                                                .endObject()
                                            .endObjectFields()
                                        .endSlot()
                                        .beginSlot()
                                        .endSlot()
                                    .endSlots()
                                .endObject()
                                .beginSerializableObject()
                                    .beginDescriptorHierarchy()
                                    .endDescriptorHierarchyWithHandle(proxyDescHandle)
                                    .beginSlots()
                                        .beginSlot()
                                            .beginObjectFields()
                                                .beginSerializableObject()
                                                    .beginDescriptorHierarchy()
                                                    .endDescriptorHierarchyWithHandle(descHandle)
                                                .endObject()
                                            .endObjectFields()
                                        .endSlot()
                                        .beginSlot()
                                        .endSlot()
                                    .endSlots()
                                .endObject()
                                // --------------------
                            .endElements()
                        .endArray()
                    .endObjectFields()
                .endSlot()
            .endSlots()
        .endObject();

        byte[] expectedData = serialize(new ClassWithHandles());
        assertArrayEquals(expectedData, actualData);
    }

    private static class SerializableWithSelfReference implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        public SerializableWithSelfReference self;
    }

    @Test
    void handle_SerializableSelfReference() {
        Handle selfHandle = new Handle();
        byte[] actualData = SerialBuilder.startSerializableObject(selfHandle)
            .beginDescriptorHierarchy()
                .beginDescriptor()
                    .typeWithUid(SerializableWithSelfReference.class)
                    .flags(SC_SERIALIZABLE)
                    .beginObjectFieldDescriptors()
                        .objectField("self", SerializableWithSelfReference.class)
                    .endObjectFieldDescriptors()
                .endDescriptor()
            .endDescriptorHierarchy()
            .beginSlots()
                .beginSlot()
                    .beginObjectFields()
                        .objectHandle(selfHandle)
                    .endObjectFields()
                .endSlot()
            .endSlots()
        .endObject();

        SerializableWithSelfReference object = new SerializableWithSelfReference();
        object.self = object;
        byte[] expectedData = serialize(object);
        assertArrayEquals(expectedData, actualData);
    }

    private static class ExternalizableWithSelfReference implements Externalizable {
        @Serial
        private static final long serialVersionUID = 1L;

        public ExternalizableWithSelfReference() {
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeObject(this);
        }

        @Override
        public void readExternal(ObjectInput in) {
            fail("Reading is not supported");
        }
    }

    @Test
    void handle_ExternalizableSelfReference() {
        Handle selfHandle = new Handle();
        byte[] actualData = SerialBuilder.startExternalizableObject(selfHandle)
            .beginDescriptorHierarchy()
                .beginDescriptor()
                    .typeWithUid(ExternalizableWithSelfReference.class)
                    .flags(SC_EXTERNALIZABLE | SC_BLOCK_DATA)
                .endDescriptor()
            .endDescriptorHierarchy()
            .writeExternalWith(writer -> writer.objectHandle(selfHandle))
        .endObject();
        
        byte[] expectedData = serialize(new ExternalizableWithSelfReference());
        assertArrayEquals(expectedData, actualData);
    }

    /**
     * Utility method to create a hex string in the format expected by https://github.com/NickstaDB/SerializationDumper
     */
    @SuppressWarnings("unused")
    private static String toHex(byte[] bytes) {
        char[] hexChars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(hexChars[(b >> 4) & 0xF]);
            sb.append(hexChars[b & 0xF]);
        }
        return sb.toString();
    }
}
