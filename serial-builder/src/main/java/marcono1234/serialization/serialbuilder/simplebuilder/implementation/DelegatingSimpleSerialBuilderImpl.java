package marcono1234.serialization.serialbuilder.simplebuilder.implementation;

import marcono1234.serialization.serialbuilder.builder.api.Enclosing;
import marcono1234.serialization.serialbuilder.builder.api.Handle;
import marcono1234.serialization.serialbuilder.builder.api.ThrowingConsumer;
import marcono1234.serialization.serialbuilder.builder.api.object.array.ArrayElements;
import marcono1234.serialization.serialbuilder.builder.api.object.serializable.SlotEnd;
import marcono1234.serialization.serialbuilder.builder.api.object.serializable.SlotPrimitiveFields;
import marcono1234.serialization.serialbuilder.simplebuilder.api.ObjectBuildingDataOutput;
import marcono1234.serialization.serialbuilder.simplebuilder.api.object.ObjectStart;
import marcono1234.serialization.serialbuilder.simplebuilder.api.object.array.ObjectArrayElements;
import marcono1234.serialization.serialbuilder.simplebuilder.api.object.proxy.ProxyObjectEnd;
import marcono1234.serialization.serialbuilder.simplebuilder.api.object.proxy.ProxyObjectStart;
import marcono1234.serialization.serialbuilder.simplebuilder.api.object.serializable.SerializableObjectData;
import marcono1234.serialization.serialbuilder.simplebuilder.api.object.serializable.SerializableObjectDataEnd;
import marcono1234.serialization.serialbuilder.simplebuilder.api.object.serializable.SerializableObjectObjectFieldEnd;
import marcono1234.serialization.serialbuilder.simplebuilder.api.object.serializable.SerializableObjectStart;
import marcono1234.serialization.serialbuilder.simplebuilder.api.object.serializable.SerializableObjectWithDataStart;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static java.io.ObjectStreamConstants.SC_BLOCK_DATA;
import static java.io.ObjectStreamConstants.SC_ENUM;
import static java.io.ObjectStreamConstants.SC_EXTERNALIZABLE;
import static java.io.ObjectStreamConstants.SC_SERIALIZABLE;
import static java.io.ObjectStreamConstants.SC_WRITE_METHOD;

@SuppressWarnings("rawtypes")
public class DelegatingSimpleSerialBuilderImpl<C> implements ObjectStart, ObjectArrayElements, SerializableObjectStart, SerializableObjectWithDataStart, SerializableObjectData, SerializableObjectObjectFieldEnd, ProxyObjectStart, ProxyObjectEnd, Enclosing {
    private final marcono1234.serialization.serialbuilder.builder.api.object.ObjectStart<C> delegateBuilder;

    /*
     * Implementation note:
     * These checks for correct ObjectBuildingDataOutput usage are done here for the simple builder as well because
     * the checks in the underlying non-simple builder might not catch every incorrect usage because some simple
     * build calls only have an effect when the builder call chain is completed.
     *
     * The implementation here differs from the one in the non-simple builder because here for ObjectBuildingDataOutput
     * a new DelegatingSimpleSerialBuilderImpl is created every time.
     */
    /**
     * Incremented when an object with multiple steps is started, and decremented when the object is finished.
     */
    private int nestingDepth = 0;
    /**
     * Whether this builder is in the current active scope. This is used to detect erroneous usage of the wrong
     * {@link ObjectBuildingDataOutput} object in case multiple are in scope (e.g. for nested Externalizable objects).
     */
    private boolean isActiveScope = true;

    public DelegatingSimpleSerialBuilderImpl(marcono1234.serialization.serialbuilder.builder.api.object.ObjectStart<C> delegateBuilder) {
        this.delegateBuilder = delegateBuilder;
    }

    private interface ObjectWriterAction {
        Object write(marcono1234.serialization.serialbuilder.builder.api.object.ObjectStart<Object> objectStart);

        /**
         * Convenience method which calls {@link #write(marcono1234.serialization.serialbuilder.builder.api.object.ObjectStart)}
         * and hides warnings about unchecked type casts. Also needed because {@code write} cannot have a type parameter
         * because lambdas cannot implement generic methods.
         */
        @SuppressWarnings("unchecked")
        default <C> C writeUnchecked(marcono1234.serialization.serialbuilder.builder.api.object.ObjectStart<? extends C> objectStart) {
            return (C) write((marcono1234.serialization.serialbuilder.builder.api.object.ObjectStart<Object>) objectStart);
        }
    }

    private final Deque<Deque<ObjectWriterAction>> pendingObjectActions = new LinkedList<>();

    protected C run(ObjectWriterAction action) {
        var actionsQueue = pendingObjectActions.peekLast();
        if (actionsQueue == null) {
            return action.writeUnchecked(delegateBuilder);
        } else {
            actionsQueue.add(action);
            return null;
        }
    }
    
    @Override
    public Object objectHandle(Handle handle) {
        Objects.requireNonNull(handle);
        run(start -> start.objectHandle(handle));
        return this;
    }

    @Override
    public Object nullObject() {
        //noinspection Convert2MethodRef
        run(start -> start.nullObject());
        return this;
    }

    @Override
    public Object string(String s) {
        Objects.requireNonNull(s);
        run(start -> start.string(s));
        return this;
    }

    private static Handle verifyUnassigned(Handle handle) {
        if (handle.isAssigned()) {
            throw new IllegalArgumentException("Handle is already assigned an object index: " + handle);
        }
        return handle;
    }

    private ArrayElements<Object> beginArray(marcono1234.serialization.serialbuilder.builder.api.object.ObjectStart<Object> start, Handle unassignedHandle, String arrayType) {
        return start.beginArray(unassignedHandle)
            .beginDescriptorHierarchy()
                .beginDescriptor()
                    .typeName(arrayType)
                    // serialVersionUID is ignored for arrays, see https://docs.oracle.com/en/java/javase/17/docs/specs/serialization/class.html#stream-unique-identifiers
                    .uid(0)
                    .flags(SC_SERIALIZABLE)
                .endDescriptor()
            .endDescriptorHierarchy();
    }

    private ArrayElements<Object> beginArray(marcono1234.serialization.serialbuilder.builder.api.object.ObjectStart<Object> start, Handle unassignedHandle, Class<?> componentType) {
        return beginArray(start, unassignedHandle, componentType.getTypeName() + "[]");
    }

    @Override
    public Object array(Handle unassignedHandle, boolean[] array) {
        verifyUnassigned(unassignedHandle);
        boolean[] arrayF = array.clone();
        run(start -> beginArray(start, unassignedHandle, boolean.class).elements(arrayF).endArray());
        return this;
    }

    @Override
    public Object array(Handle unassignedHandle, byte[] array) {
        verifyUnassigned(unassignedHandle);
        byte[] arrayF = array.clone();
        run(start -> beginArray(start, unassignedHandle, byte.class).elements(arrayF).endArray());
        return this;
    }

    @Override
    public Object array(Handle unassignedHandle, char[] array) {
        verifyUnassigned(unassignedHandle);
        char[] arrayF = array.clone();
        run(start -> beginArray(start, unassignedHandle, char.class).elements(arrayF).endArray());
        return this;
    }

    @Override
    public Object array(Handle unassignedHandle, short[] array) {
        verifyUnassigned(unassignedHandle);
        short[] arrayF = array.clone();
        run(start -> beginArray(start, unassignedHandle, short.class).elements(arrayF).endArray());
        return this;
    }

    @Override
    public Object array(Handle unassignedHandle, int[] array) {
        verifyUnassigned(unassignedHandle);
        int[] arrayF = array.clone();
        run(start -> beginArray(start, unassignedHandle, int.class).elements(arrayF).endArray());
        return this;
    }

    @Override
    public Object array(Handle unassignedHandle, long[] array) {
        verifyUnassigned(unassignedHandle);
        long[] arrayF = array.clone();
        run(start -> beginArray(start, unassignedHandle, long.class).elements(arrayF).endArray());
        return this;
    }

    @Override
    public Object array(Handle unassignedHandle, float[] array) {
        verifyUnassigned(unassignedHandle);
        float[] arrayF = array.clone();
        run(start -> beginArray(start, unassignedHandle, float.class).elements(arrayF).endArray());
        return this;
    }

    @Override
    public Object array(Handle unassignedHandle, double[] array) {
        verifyUnassigned(unassignedHandle);
        double[] arrayF = array.clone();
        run(start -> beginArray(start, unassignedHandle, double.class).elements(arrayF).endArray());
        return this;
    }

    record ObjectArrayData(Handle unassignedHandle, String arrayType) {
        ObjectArrayData {
            verifyUnassigned(unassignedHandle);
            Objects.requireNonNull(arrayType);
        }
    }

    private final Deque<ObjectArrayData> pendingObjectArrayData = new LinkedList<>();

    @Override
    public ObjectArrayElements beginObjectArray(Handle unassignedHandle, String arrayType) {
        nestingDepth++;
        pendingObjectArrayData.addLast(new ObjectArrayData(unassignedHandle, arrayType));
        pendingObjectActions.addLast(new LinkedList<>());
        return this;
    }

    @Override
    public Object endArray() {
        nestingDepth--;
        ObjectArrayData arrayData = pendingObjectArrayData.removeLast();
        Deque<ObjectWriterAction> elementActions = pendingObjectActions.removeLast();
        C result = run(start -> {
            var current = beginArray(start, arrayData.unassignedHandle, arrayData.arrayType).beginObjectElements();
            for (ObjectWriterAction elementAction : elementActions) {
                current = elementAction.writeUnchecked(current);
            }
            return current.endElements().endArray();
        });

        if (nestingDepth == 0) {
            if (!pendingObjectActions.isEmpty()) {
                throw new AssertionError("Pending object actions: " + pendingObjectActions.size());
            }
            return result;
        } else {
            return this;
        }
    }

    @Override
    public Object objectArray(Handle unassignedHandle, String arrayType, Function writer) {
        @SuppressWarnings("unchecked")
        var returnedBuilder = writer.apply(beginObjectArray(unassignedHandle, arrayType));
        if (returnedBuilder != this) {
            throw new IllegalStateException("Incorrect builder usage");
        }
        return this;
    }

    @Override
    public Object enumConstant(String enumClass, String constantName) {
        Objects.requireNonNull(enumClass);
        Objects.requireNonNull(constantName);
        run(start -> start.beginEnum()
            .beginDescriptorHierarchy()
                .beginDescriptor()
                    .typeName(enumClass)
                    // Enum classes have UID 0, see https://docs.oracle.com/en/java/javase/17/docs/specs/serialization/class.html#stream-unique-identifiers
                    .uid(0)
                    .flags(SC_SERIALIZABLE | SC_ENUM)
                .endDescriptor()
                .beginDescriptor()
                    .enumClass(Enum.class)
                    .flags(SC_SERIALIZABLE | SC_ENUM)
                .endDescriptor()
            .endDescriptorHierarchy()
            .name(constantName)
        .endEnum());
        return this;
    }
    
    private void classWithFlags(String className, long serialVersionUID, int flags) {
        Objects.requireNonNull(className);
        run(start -> {
            //noinspection CodeBlock2Expr
            return start.beginClass()
                .beginDescriptorHierarchy()
                    .beginDescriptor()
                        .typeName(className)
                        .uid(serialVersionUID)
                        .flags(flags)
                    .endDescriptor()
                .endDescriptorHierarchy()
            .endClass();
        });
    }

    @Override
    public Object nonSerializableClass(String className) {
        classWithFlags(className, 0, 0);
        return this;
    }

    @Override
    public Object externalizableClass(String className, long serialVersionUID) {
        // Only support protocol version 2 which writes Externalizable data as block data
        classWithFlags(className, serialVersionUID, SC_EXTERNALIZABLE | SC_BLOCK_DATA);
        return this;
    }

    @Override
    public Object serializableClass(String className, long serialVersionUID) {
        classWithFlags(className, serialVersionUID, SC_SERIALIZABLE);
        return this;
    }

    @Override
    public Object enumClass(String className) {
        // Enum has serialVersionUID 0
        classWithFlags(className, 0, SC_SERIALIZABLE | SC_ENUM);
        return this;
    }

    @Override
    public Object proxyClass(String... interfaceNames) {
        String[] interfaceNamesF = interfaceNames.clone();
        run(start -> {
            //noinspection CodeBlock2Expr
            return start.beginClass()
                .beginDescriptorHierarchy()
                    .proxyDescriptor(interfaceNamesF)
                .endDescriptorHierarchy()
            .endClass();
        });
        return this;
    }

    private record ClassData(String className, long serialVersionUID, List<PrimitiveFieldData> primitiveFieldDataList, Deque<ObjectFieldData> objectFieldDataList, AtomicReference<ThrowingConsumer> writeObjectWriter) {
        ClassData {
            Objects.requireNonNull(className);
            Objects.requireNonNull(primitiveFieldDataList);
            Objects.requireNonNull(objectFieldDataList);
            Objects.requireNonNull(writeObjectWriter);
        }

        ClassData(String className, long serialVersionUID) {
            this(className, serialVersionUID, new ArrayList<>(), new LinkedList<>(), new AtomicReference<>());
        }
    }

    private record PrimitiveFieldData(String fieldName, Class<?> fieldType, UnaryOperator<SlotPrimitiveFields<Object>> valueWriter) {
        PrimitiveFieldData {
            Objects.requireNonNull(fieldName);
            Objects.requireNonNull(fieldType);
            Objects.requireNonNull(valueWriter);

            if (!fieldType.isPrimitive()) {
                throw new IllegalArgumentException("Not a primitive type: " + fieldType.getTypeName());
            }
        }
    }

    private record ObjectFieldData(String fieldName, String fieldType, AtomicReference<ObjectWriterAction> objectWriterAction) {
        ObjectFieldData {
            Objects.requireNonNull(fieldName);
            Objects.requireNonNull(fieldType);
            Objects.requireNonNull(objectWriterAction);
        }

        ObjectFieldData(String fieldName, String fieldType) {
            this(fieldName, fieldType, new AtomicReference<>());
        }
    }

    private final Deque<Handle> currentSerializableObjectHandle = new LinkedList<>();
    private final Deque<Deque<ClassData>> currentSerializableClassDataList = new LinkedList<>();

    @Override
    public SerializableObjectStart beginSerializableObject(Handle unassignedHandle) {
        nestingDepth++;
        currentSerializableObjectHandle.addLast(verifyUnassigned(unassignedHandle));
        pendingObjectActions.addLast(new LinkedList<>());
        currentSerializableClassDataList.addLast(new LinkedList<>());
        return this;
    }

    @Override
    public SerializableObjectData beginClassData(String className, long serialVersionUID) {
        Objects.requireNonNull(className);
        currentSerializableClassDataList.getLast().add(new ClassData(className, serialVersionUID));
        return this;
    }

    @Override
    public SerializableObjectData primitiveBooleanField(String fieldName, boolean value) {
        Objects.requireNonNull(fieldName);
        currentSerializableClassDataList.getLast().getLast().primitiveFieldDataList.add(new PrimitiveFieldData(fieldName, boolean.class, primitiveFields -> primitiveFields.booleanValue(value)));
        return this;
    }

    @Override
    public SerializableObjectData primitiveByteField(String fieldName, byte value) {
        Objects.requireNonNull(fieldName);
        currentSerializableClassDataList.getLast().getLast().primitiveFieldDataList.add(new PrimitiveFieldData(fieldName, byte.class, primitiveFields -> primitiveFields.byteValue(value)));
        return this;
    }

    @Override
    public SerializableObjectData primitiveCharField(String fieldName, char value) {
        Objects.requireNonNull(fieldName);
        currentSerializableClassDataList.getLast().getLast().primitiveFieldDataList.add(new PrimitiveFieldData(fieldName, char.class, primitiveFields -> primitiveFields.charValue(value)));
        return this;
    }

    @Override
    public SerializableObjectData primitiveShortField(String fieldName, short value) {
        Objects.requireNonNull(fieldName);
        currentSerializableClassDataList.getLast().getLast().primitiveFieldDataList.add(new PrimitiveFieldData(fieldName, short.class, primitiveFields -> primitiveFields.shortValue(value)));
        return this;
    }

    @Override
    public SerializableObjectData primitiveIntField(String fieldName, int value) {
        Objects.requireNonNull(fieldName);
        currentSerializableClassDataList.getLast().getLast().primitiveFieldDataList.add(new PrimitiveFieldData(fieldName, int.class, primitiveFields -> primitiveFields.intValue(value)));
        return this;
    }

    @Override
    public SerializableObjectData primitiveLongField(String fieldName, long value) {
        Objects.requireNonNull(fieldName);
        currentSerializableClassDataList.getLast().getLast().primitiveFieldDataList.add(new PrimitiveFieldData(fieldName, long.class, primitiveFields -> primitiveFields.longValue(value)));
        return this;
    }

    @Override
    public SerializableObjectData primitiveFloatField(String fieldName, float value) {
        Objects.requireNonNull(fieldName);
        currentSerializableClassDataList.getLast().getLast().primitiveFieldDataList.add(new PrimitiveFieldData(fieldName, float.class, primitiveFields -> primitiveFields.floatValue(value)));
        return this;
    }

    @Override
    public SerializableObjectData primitiveDoubleField(String fieldName, double value) {
        Objects.requireNonNull(fieldName);
        currentSerializableClassDataList.getLast().getLast().primitiveFieldDataList.add(new PrimitiveFieldData(fieldName, double.class, primitiveFields -> primitiveFields.doubleValue(value)));
        return this;
    }

    @Override
    public ObjectStart beginObjectField(String fieldName, String fieldType) {
        Objects.requireNonNull(fieldName);
        Objects.requireNonNull(fieldType);
        currentSerializableClassDataList.getLast().getLast().objectFieldDataList.add(new ObjectFieldData(fieldName, fieldType));
        return this;
    }

    @Override
    public SerializableObjectData endField() {
        // When ending object field store the writer action in the class data
        ObjectWriterAction objectWriterAction = pendingObjectActions.getLast().removeFirst();
        currentSerializableClassDataList.getLast().getLast().objectFieldDataList.getLast().objectWriterAction().set(objectWriterAction);
        return this;
    }

    @Override
    public SerializableObjectData objectField(String fieldName, String fieldType, Function writer) {
        @SuppressWarnings("unchecked")
        var returnedBuilder = writer.apply(beginObjectField(fieldName, fieldType));
        if (returnedBuilder != this) {
            throw new IllegalStateException("Incorrect builder usage");
        }
        return this.endField();
    }

    /**
     * Allows delegating calls on a {@link ObjectBuildingDataOutput} to a non-simple {@link marcono1234.serialization.serialbuilder.builder.api.ObjectBuildingDataOutput}.
     * Once a non-simple {@code ObjectBuildingDataOutput} is passed to the returned {@code ThrowingConsumer}, it creates
     * a simple {@code ObjectBuildingDataOutput} based on it which is then itself passed to the provided {@code writer}.
     *
     * @param enclosing
     *      the enclosing builder for which the data output is created; may be {@code null}
     * @param writer
     *      writing to a simple {@code ObjectBuildingDataOutput}
     * @return
     *      consumer of a non-simple {@code ObjectBuildingDataOutput}, which, when called, passes a simple
     *      {@code ObjectBuildingDataOutput} to the {@code writer}
     */
    protected static ThrowingConsumer<marcono1234.serialization.serialbuilder.builder.api.ObjectBuildingDataOutput> createDataOutputConsumer(DelegatingSimpleSerialBuilderImpl<?> enclosing, ThrowingConsumer<ObjectBuildingDataOutput> writer) {
        Objects.requireNonNull(writer);

        return nonSimpleWriter -> {
            DelegatingSimpleSerialBuilderImpl<Void> delegateObjectBuilder = new DelegatingSimpleSerialBuilderImpl<>(nonSimpleWriter);
            int originalNestingDepth = delegateObjectBuilder.nestingDepth;
            ObjectBuildingDataOutput dataOutput = new ObjectBuildingDataOutput() {
                private void verifyOutputIsUsable() {
                    if (!delegateObjectBuilder.isActiveScope) {
                        throw new IllegalStateException("Other output is currently active; make sure you called the method on the correct ObjectBuildingDataOutput variable");
                    }
                    if (delegateObjectBuilder.nestingDepth != originalNestingDepth) {
                        throw new IllegalStateException("Previous builder call is incomplete; make sure all builder methods are called until the return type is Void");
                    }
                }

                @Override
                public void write(int b) throws IOException {
                    verifyOutputIsUsable();
                    nonSimpleWriter.write(b);
                }

                @Override
                public void write(byte[] b) throws IOException {
                    verifyOutputIsUsable();
                    nonSimpleWriter.write(b);
                }

                @Override
                public void write(byte[] b, int off, int len) throws IOException {
                    verifyOutputIsUsable();
                    nonSimpleWriter.write(b, off, len);
                }

                @Override
                public void writeBoolean(boolean v) throws IOException {
                    verifyOutputIsUsable();
                    nonSimpleWriter.writeBoolean(v);
                }

                @Override
                public void writeByte(int v) throws IOException {
                    verifyOutputIsUsable();
                    nonSimpleWriter.writeByte(v);
                }

                @Override
                public void writeShort(int v) throws IOException {
                    verifyOutputIsUsable();
                    nonSimpleWriter.writeShort(v);
                }

                @Override
                public void writeChar(int v) throws IOException {
                    verifyOutputIsUsable();
                    nonSimpleWriter.writeChar(v);
                }

                @Override
                public void writeInt(int v) throws IOException {
                    verifyOutputIsUsable();
                    nonSimpleWriter.writeInt(v);
                }

                @Override
                public void writeLong(long v) throws IOException {
                    verifyOutputIsUsable();
                    nonSimpleWriter.writeLong(v);
                }

                @Override
                public void writeFloat(float v) throws IOException {
                    verifyOutputIsUsable();
                    nonSimpleWriter.writeFloat(v);
                }

                @Override
                public void writeDouble(double v) throws IOException {
                    verifyOutputIsUsable();
                    nonSimpleWriter.writeDouble(v);
                }

                @Deprecated
                @Override
                public void writeBytes(String s) throws IOException {
                    verifyOutputIsUsable();
                    nonSimpleWriter.writeBytes(s);
                }

                @Override
                public void writeChars(String s) throws IOException {
                    verifyOutputIsUsable();
                    nonSimpleWriter.writeChars(s);
                }

                @Override
                public void writeUTF(String s) throws IOException {
                    verifyOutputIsUsable();
                    nonSimpleWriter.writeUTF(s);
                }

                // ObjectStart methods

                @Override
                public Void objectHandle(Handle handle) {
                    verifyOutputIsUsable();
                    delegateObjectBuilder.objectHandle(handle);
                    return null;
                }

                @Override
                public Void nullObject() {
                    verifyOutputIsUsable();
                    delegateObjectBuilder.nullObject();
                    return null;
                }

                @Override
                public Void string(String s) {
                    verifyOutputIsUsable();
                    delegateObjectBuilder.string(s);
                    return null;
                }

                @Override
                public Void array(Handle unassignedHandle, boolean[] array) {
                    verifyOutputIsUsable();
                    delegateObjectBuilder.array(unassignedHandle, array);
                    return null;
                }

                @Override
                public Void array(Handle unassignedHandle, byte[] array) {
                    verifyOutputIsUsable();
                    delegateObjectBuilder.array(unassignedHandle, array);
                    return null;
                }

                @Override
                public Void array(Handle unassignedHandle, char[] array) {
                    verifyOutputIsUsable();
                    delegateObjectBuilder.array(unassignedHandle, array);
                    return null;
                }

                @Override
                public Void array(Handle unassignedHandle, short[] array) {
                    verifyOutputIsUsable();
                    delegateObjectBuilder.array(unassignedHandle, array);
                    return null;
                }

                @Override
                public Void array(Handle unassignedHandle, int[] array) {
                    verifyOutputIsUsable();
                    delegateObjectBuilder.array(unassignedHandle, array);
                    return null;
                }

                @Override
                public Void array(Handle unassignedHandle, long[] array) {
                    verifyOutputIsUsable();
                    delegateObjectBuilder.array(unassignedHandle, array);
                    return null;
                }

                @Override
                public Void array(Handle unassignedHandle, float[] array) {
                    verifyOutputIsUsable();
                    delegateObjectBuilder.array(unassignedHandle, array);
                    return null;
                }

                @Override
                public Void array(Handle unassignedHandle, double[] array) {
                    verifyOutputIsUsable();
                    delegateObjectBuilder.array(unassignedHandle, array);
                    return null;
                }

                @SuppressWarnings("unchecked")
                @Override
                public ObjectArrayElements<Void> beginObjectArray(Handle unassignedHandle, String arrayType) {
                    verifyOutputIsUsable();
                    return delegateObjectBuilder.beginObjectArray(unassignedHandle, arrayType);
                }

                @Override
                public Void objectArray(Handle unassignedHandle, String arrayType, Function<ObjectArrayElements<Enclosing>, Enclosing> writer) {
                    verifyOutputIsUsable();
                    delegateObjectBuilder.objectArray(unassignedHandle, arrayType, writer);
                    return null;
                }

                @Override
                public Void enumConstant(String enumClass, String constantName) {
                    verifyOutputIsUsable();
                    delegateObjectBuilder.enumConstant(enumClass, constantName);
                    return null;
                }

                @Override
                public Void nonSerializableClass(String className) {
                    verifyOutputIsUsable();
                    delegateObjectBuilder.nonSerializableClass(className);
                    return null;
                }

                @Override
                public Void externalizableClass(String className, long serialVersionUID) {
                    verifyOutputIsUsable();
                    delegateObjectBuilder.externalizableClass(className, serialVersionUID);
                    return null;
                }

                @Override
                public Void serializableClass(String className, long serialVersionUID) {
                    verifyOutputIsUsable();
                    delegateObjectBuilder.serializableClass(className, serialVersionUID);
                    return null;
                }

                @Override
                public Void enumClass(String className) {
                    verifyOutputIsUsable();
                    delegateObjectBuilder.enumClass(className);
                    return null;
                }

                @Override
                public Void proxyClass(String... interfaceNames) {
                    verifyOutputIsUsable();
                    delegateObjectBuilder.proxyClass(interfaceNames);
                    return null;
                }

                @SuppressWarnings("unchecked")
                @Override
                public SerializableObjectStart<Void> beginSerializableObject(Handle unassignedHandle) {
                    verifyOutputIsUsable();
                    return delegateObjectBuilder.beginSerializableObject(unassignedHandle);
                }

                @Override
                public Void serializableObject(Handle unassignedHandle, Function writer) {
                    verifyOutputIsUsable();
                    delegateObjectBuilder.serializableObject(unassignedHandle, writer);
                    return null;
                }

                @Override
                public Void externalizableObject(Handle unassignedHandle, String typeName, long serialVersionUID, ThrowingConsumer<ObjectBuildingDataOutput> writer) {
                    verifyOutputIsUsable();
                    delegateObjectBuilder.externalizableObject(unassignedHandle, typeName, serialVersionUID, writer);
                    return null;
                }

                @SuppressWarnings("unchecked")
                @Override
                public ProxyObjectStart<ProxyObjectEnd<Void>> beginProxyObject(Handle unassignedHandle, String... interfaceNames) {
                    verifyOutputIsUsable();
                    return delegateObjectBuilder.beginProxyObject(unassignedHandle, interfaceNames);
                }

                @Override
                public Void proxyObject(Handle unassignedHandle, String[] interfaceNames, Function<ProxyObjectStart<Enclosing>, Enclosing> writer) {
                    verifyOutputIsUsable();
                    delegateObjectBuilder.proxyObject(unassignedHandle, interfaceNames, writer);
                    return null;
                }
            };

            // Mark the enclosing builder as not active
            if (enclosing != null) {
                enclosing.isActiveScope = false;
            }

            writer.accept(dataOutput);
            if (delegateObjectBuilder.nestingDepth != originalNestingDepth) {
                throw new IllegalStateException("Usage of ObjectBuildingDataOutput did not complete builder call; make sure all builder methods are called until the return type is Void");
            }

            // Mark the enclosing builder as active again
            if (enclosing != null) {
                enclosing.isActiveScope = true;
            }
        };
    }
    
    @Override
    public SerializableObjectDataEnd writeObjectWith(ThrowingConsumer writer) {
        Objects.requireNonNull(writer);
        currentSerializableClassDataList.getLast().getLast().writeObjectWriter.set(writer);
        return this;
    }

    @Override
    public Object endClassData() {
        return this;
    }

    @Override
    public Object endObject() {
        nestingDepth--;
        var remainingActions = pendingObjectActions.removeLast();
        if (!remainingActions.isEmpty()) {
            throw new IllegalStateException("Unexpected remaining object actions: " + remainingActions);
        }

        Handle unassignedHandle = currentSerializableObjectHandle.removeLast();
        Deque<ClassData> classDataList = currentSerializableClassDataList.removeLast();
        Object result = run(start -> {
            var descriptorsList = start.beginSerializableObject(unassignedHandle).beginDescriptorHierarchy();
            // Descriptor hierarchy is written in reverse order
            for (ClassData classData : (Iterable<ClassData>) classDataList::descendingIterator) {
                @SuppressWarnings("unchecked")
                ThrowingConsumer<ObjectBuildingDataOutput> writeObjectWriter = classData.writeObjectWriter.get();
                int flags = SC_SERIALIZABLE;
                if (writeObjectWriter != null) {
                    flags |= SC_WRITE_METHOD;
                }

                var primitiveFieldDescriptors = descriptorsList.beginDescriptor()
                    .typeName(classData.className)
                    .uid(classData.serialVersionUID)
                    .flags(flags)
                    .beginPrimitiveFieldDescriptors();

                for (PrimitiveFieldData primitiveFieldData : classData.primitiveFieldDataList) {
                    primitiveFieldDescriptors = primitiveFieldDescriptors.primitiveField(primitiveFieldData.fieldName, primitiveFieldData.fieldType);
                }

                var objectFieldDescriptors = primitiveFieldDescriptors.endPrimitiveFieldDescriptors().beginObjectFieldDescriptors();
                for (ObjectFieldData objectFieldData : classData.objectFieldDataList) {
                    objectFieldDescriptors = objectFieldDescriptors.objectField(objectFieldData.fieldName, objectFieldData.fieldType);
                }

                descriptorsList = objectFieldDescriptors.endObjectFieldDescriptors().endDescriptor();
            }

            var slotsStart = descriptorsList.endDescriptorHierarchy().beginSlots();
            for (ClassData classData : classDataList) {
                var primitiveFieldValues = slotsStart.beginSlot().beginPrimitiveFields();
                for (PrimitiveFieldData primitiveFieldData : classData.primitiveFieldDataList) {
                    primitiveFieldValues = primitiveFieldData.valueWriter().apply(primitiveFieldValues);
                }

                var objectFieldValues = primitiveFieldValues.endPrimitiveFields().beginObjectFields();
                for (ObjectFieldData objectFieldData : classData.objectFieldDataList) {
                    objectFieldValues = objectFieldData.objectWriterAction().get().writeUnchecked(objectFieldValues);
                }

                var afterObjectFields = objectFieldValues.endObjectFields();
                SlotEnd<Object> slotEnd = afterObjectFields;
                @SuppressWarnings("unchecked")
                ThrowingConsumer<ObjectBuildingDataOutput> writeObjectWriter = classData.writeObjectWriter.get();
                if (writeObjectWriter != null) {
                    slotEnd = afterObjectFields.writeObjectWith(createDataOutputConsumer(this, writeObjectWriter));
                }

                slotsStart = slotEnd.endSlot();
            }

            return slotsStart.endSlots().endObject();
        });

        if (nestingDepth == 0) {
            if (!pendingObjectActions.isEmpty()) {
                throw new AssertionError("Pending object actions: " + pendingObjectActions.size());
            }
            return result;
        } else {
            return this;
        }
    }

    @Override
    public Object serializableObject(Handle unassignedHandle, Function writer) {
        @SuppressWarnings("unchecked")
        var returnedBuilder = writer.apply(beginSerializableObject(unassignedHandle));
        if (returnedBuilder != this) {
            throw new IllegalStateException("Incorrect builder usage");
        }
        return this;
    }

    @Override
    public Object externalizableObject(Handle unassignedHandle, String typeName, long serialVersionUID, ThrowingConsumer writer) {
        verifyUnassigned(unassignedHandle);
        Objects.requireNonNull(typeName);
        @SuppressWarnings("unchecked")
        ThrowingConsumer<ObjectBuildingDataOutput> writerT = (ThrowingConsumer<ObjectBuildingDataOutput>) Objects.requireNonNull(writer);
        C result = run(start -> {
            //noinspection CodeBlock2Expr
            return start.beginExternalizableObject(unassignedHandle)
                .beginDescriptorHierarchy()
                    .beginDescriptor()
                        .typeName(typeName)
                        .uid(serialVersionUID)
                        // Only support protocol version 2 which writes Externalizable data as block data
                        .flags(SC_EXTERNALIZABLE | SC_BLOCK_DATA)
                    .endDescriptor()
                .endDescriptorHierarchy()
                .writeExternalWith(createDataOutputConsumer(this, writerT))
            .endObject();
        });

        if (nestingDepth == 0) {
            if (!pendingObjectActions.isEmpty()) {
                throw new AssertionError("Pending object actions: " + pendingObjectActions.size());
            }
            return result;
        } else {
            return this;
        }
    }

    private record ProxyData(Handle unassignedHandle, String[] interfaceNames) {
        ProxyData {
            verifyUnassigned(unassignedHandle);
            Objects.requireNonNull(interfaceNames);
        }
    }

    private final Deque<ProxyData> currentProxyData = new LinkedList<>();

    private C proxyObject(ObjectWriterAction invocationHandlerWriter) {
        Objects.requireNonNull(invocationHandlerWriter);
        
        ProxyData proxyData = currentProxyData.removeLast();
        return run(start -> {
            var current = start.beginSerializableObject(proxyData.unassignedHandle)
                .beginDescriptorHierarchy()
                    .proxyDescriptor(proxyData.interfaceNames)
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
                        .beginObjectFields();
            current = invocationHandlerWriter.writeUnchecked(current);
            return current.endObjectFields()
                    .endSlot()
                    .beginSlot()
                    .endSlot()
                .endSlots()
            .endObject();
        });
    }
    
    @Override
    public ProxyObjectStart beginProxyObject(Handle unassignedHandle, String... interfaceNames) {
        nestingDepth++;
        currentProxyData.addLast(new ProxyData(unassignedHandle, interfaceNames.clone()));
        // Add new object actions nesting level for invocation handler object
        pendingObjectActions.addLast(new LinkedList<>());
        return this;
    }

    @Override
    public ProxyObjectEnd invocationHandlerHandle(Handle handle) {
        run(objectStart -> objectStart.objectHandle(handle));
        return this;
    }

    @Override
    public ProxyObjectStart beginProxyInvocationHandler(Handle unassignedHandle, String... interfaceNames) {
        return beginProxyObject(unassignedHandle, interfaceNames);
    }

    @Override
    public SerializableObjectStart beginSerializableInvocationHandler(Handle unassignedHandle) {
        return beginSerializableObject(unassignedHandle);
    }

    @Override
    public ProxyObjectEnd externalizableInvocationHandler(Handle unassignedHandle, String typeName, long serialVersionUID, ThrowingConsumer writer) {
        externalizableObject(unassignedHandle, typeName, serialVersionUID, writer);
        return this;
    }

    @Override
    public Object endProxyObject() {
        nestingDepth--;
        Deque<ObjectWriterAction> invocationHandlerActions = pendingObjectActions.removeLast();
        if (invocationHandlerActions.size() != 1) {
            throw new IllegalStateException("Expected one invocation handler action, but got: " + invocationHandlerActions);
        }
        C result = proxyObject(invocationHandlerActions.getFirst());
        if (nestingDepth == 0) {
            if (!pendingObjectActions.isEmpty()) {
                throw new AssertionError("Pending object actions: " + pendingObjectActions.size());
            }
            return result;
        } else {
            return this;
        }
    }

    @Override
    public Object proxyObject(Handle unassignedHandle, String[] interfaceNames, Function writer) {
        @SuppressWarnings("unchecked")
        var returnedBuilder = writer.apply(beginProxyObject(unassignedHandle, interfaceNames));
        if (returnedBuilder != this) {
            throw new IllegalStateException("Incorrect builder usage");
        }
        return this.endProxyObject();
    }
}
