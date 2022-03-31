package marcono1234.serialization.serialbuilder.builder.implementation;

import marcono1234.serialization.serialbuilder.SerialBuilder;
import marcono1234.serialization.serialbuilder.builder.api.Enclosing;
import marcono1234.serialization.serialbuilder.builder.api.Handle;
import marcono1234.serialization.serialbuilder.builder.api.ObjectBuildingDataOutput;
import marcono1234.serialization.serialbuilder.builder.api.ThrowingConsumer;
import marcono1234.serialization.serialbuilder.builder.api.descriptor.DescriptorHierarchyStart;
import marcono1234.serialization.serialbuilder.builder.api.descriptor.DescriptorsList;
import marcono1234.serialization.serialbuilder.builder.api.object.ClassEnd;
import marcono1234.serialization.serialbuilder.builder.api.object.EnumEnd;
import marcono1234.serialization.serialbuilder.builder.api.object.EnumStart;
import marcono1234.serialization.serialbuilder.builder.api.object.ObjectEnd;
import marcono1234.serialization.serialbuilder.builder.api.object.ObjectStart;
import marcono1234.serialization.serialbuilder.builder.api.object.array.ArrayElements;
import marcono1234.serialization.serialbuilder.builder.api.object.array.ArrayEnd;
import marcono1234.serialization.serialbuilder.builder.api.object.array.ArrayObjectElementsStart;
import marcono1234.serialization.serialbuilder.builder.api.object.externalizable.ExternalizableObjectStart;
import marcono1234.serialization.serialbuilder.builder.api.object.serializable.SerializableObjectStart;
import marcono1234.serialization.serialbuilder.builder.api.object.serializable.SlotEnd;
import marcono1234.serialization.serialbuilder.builder.api.object.serializable.SlotObjectFields;
import marcono1234.serialization.serialbuilder.builder.api.object.serializable.SlotObjectFieldsStart;
import marcono1234.serialization.serialbuilder.builder.api.object.serializable.SlotPrimitiveFields;
import marcono1234.serialization.serialbuilder.builder.api.object.serializable.SlotStart;
import marcono1234.serialization.serialbuilder.builder.api.object.serializable.SlotWriteObjectMethodData;
import marcono1234.serialization.serialbuilder.builder.api.object.serializable.SlotsStart;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static java.io.ObjectStreamConstants.STREAM_MAGIC;
import static java.io.ObjectStreamConstants.STREAM_VERSION;
import static java.io.ObjectStreamConstants.TC_ARRAY;
import static java.io.ObjectStreamConstants.TC_CLASS;
import static java.io.ObjectStreamConstants.TC_ENDBLOCKDATA;
import static java.io.ObjectStreamConstants.TC_ENUM;
import static java.io.ObjectStreamConstants.TC_NULL;
import static java.io.ObjectStreamConstants.TC_OBJECT;
import static java.io.ObjectStreamConstants.TC_REFERENCE;
import static java.io.ObjectStreamConstants.baseWireHandle;

@SuppressWarnings("rawtypes")
public class SerialBuilderImpl implements ObjectStart, ArrayObjectElementsStart, ArrayElements, ArrayEnd, EnumStart, EnumEnd, ClassEnd, SerializableObjectStart, ExternalizableObjectStart, SlotsStart, SlotStart, SlotPrimitiveFields, SlotObjectFieldsStart, SlotObjectFields, SlotWriteObjectMethodData, ObjectEnd, Enclosing {
    private enum ProtocolVersion {
        /** {@link java.io.ObjectStreamConstants#PROTOCOL_VERSION_1} */
        @SuppressWarnings("unused")
        V1,
        /** {@link java.io.ObjectStreamConstants#PROTOCOL_VERSION_2} */
        V2,
    }

    private final ProtocolVersion protocolVersion;
    /**
     * Whether this builder is building a single object, or potentially multiple objects; see
     * {@link #writeSerializationDataWith(ThrowingConsumer)}.
     */
    private final boolean isBuildingSingleObject;
    private final ByteArrayOutputStream binaryOut;
    private final UncheckedBlockDataOutputStream out;
    private final AtomicInteger nextHandleIndex = new AtomicInteger(0);

    /**
     * Incremented when an object with multiple steps is started, and decremented when the object is finished.
     */
    private int nestingDepth = 0;

    /**
     * Queue for actions to perform after that data of an object has been written. Objects push one action which
     * they then later pop again and execute.
     *
     * <p>A queue is used to support nested objects.
     */
    private final Deque<Runnable> pendingPostObjectActions = new LinkedList<>();

    /**
     * Used to collect actions when the number of actions has to be persisted beforehand, e.g. when collection the
     * array elements.
     *
     * <p>A queue is used to support nested objects.
     */
    private final Deque<List<Runnable>> pendingObjectsActions = new LinkedList<>();

    private SerialBuilderImpl(boolean isBuildingSingleObject) {
        this.isBuildingSingleObject = isBuildingSingleObject;
        protocolVersion = ProtocolVersion.V2;
        binaryOut = new ByteArrayOutputStream();
        out = new UncheckedBlockDataOutputStream(binaryOut);

        out.writeShort(STREAM_MAGIC);
        out.writeShort(STREAM_VERSION);
    }

    private void run(Runnable r) {
        var actionsQueue = pendingObjectsActions.peekLast();
        if (actionsQueue == null) {
            r.run();
        } else {
            actionsQueue.add(r);
        }
    }

    private final Deque<AtomicInteger> objectArrayElementCounts = new LinkedList<>();

    private void onStartedObject(boolean canBeNested) {
        AtomicInteger arrayElementsCount = objectArrayElementCounts.peekLast();
        if (arrayElementsCount != null) {
            arrayElementsCount.incrementAndGet();
        }

        // If object can itself have objects, record a dummy elements count to avoid having them
        // influence array elements count
        if (canBeNested) {
            objectArrayElementCounts.addLast(null);
        }
    }

    @Override
    public Object objectHandle(Handle handle) {
        int handleIndex = HandleAccess.INSTANCE.getObjectIndex(handle);
        run(() -> {
            boolean oldMode = out.setBlockDataMode(false);
            out.writeByte(TC_REFERENCE);
            out.writeInt(baseWireHandle + handleIndex);
            out.setBlockDataMode(oldMode);
        });
        onStartedObject(false);
        return this;
    }

    @Override
    public Object nullObject() {
        run(() -> {
            boolean oldMode = out.setBlockDataMode(false);
            out.writeByte(TC_NULL);
            out.setBlockDataMode(oldMode);
        });
        onStartedObject(false);
        return this;
    }

    @Override
    public Object string(Handle unassignedHandle, String s) {
        Objects.requireNonNull(s);
        int handleIndex = nextHandleIndex.getAndIncrement();
        HandleAccess.INSTANCE.assignIndex(unassignedHandle, handleIndex);

        run(() -> {
            boolean oldMode = out.setBlockDataMode(false);
            out.writeSerialString(s);
            out.setBlockDataMode(oldMode);
        });
        onStartedObject(false);
        return this;
    }

    private DescriptorHierarchyBuilderImpl createDescriptorHierarchyBuilder(Handle postDescriptorHierarchyHandle) {
        return new DescriptorHierarchyBuilderImpl(this, nextHandleIndex, postDescriptorHierarchyHandle, action -> run(() -> action.accept(out)));
    }

    @Override
    public DescriptorHierarchyStart<ArrayElements> beginArray(Handle unassignedHandle) {
        Objects.requireNonNull(unassignedHandle);
        nestingDepth++;

        AtomicBoolean oldMode = new AtomicBoolean();
        run(() -> {
            boolean oldModeValue = out.setBlockDataMode(false);
            oldMode.set(oldModeValue);
            out.writeByte(TC_ARRAY);
        });
        pendingPostObjectActions.addLast(() -> out.setBlockDataMode(oldMode.get()));
        onStartedObject(true);

        @SuppressWarnings("unchecked")
        DescriptorHierarchyStart<ArrayElements> result = createDescriptorHierarchyBuilder(unassignedHandle);
        return result;
    }

    @Override
    public Object array(Handle unassignedHandle, Function writer) {
        @SuppressWarnings("unchecked")
        var returnedBuilder = writer.apply(beginArray(unassignedHandle));
        if (returnedBuilder != this) {
            throw new IllegalStateException("Incorrect builder usage");
        }

        return this;
    }

    @Override
    public ArrayEnd elements(boolean[] array) {
        // Clone array to prevent accidental modification in the meantime
        boolean[] arrayCloned = array.clone();
        run(() -> out.writeSerialArray(arrayCloned));
        return this;
    }

    @Override
    public ArrayEnd elements(byte[] array) {
        // Clone array to prevent accidental modification in the meantime
        byte[] arrayCloned = array.clone();
        run(() -> out.writeSerialArray(arrayCloned));
        return this;
    }

    @Override
    public ArrayEnd elements(char[] array) {
        // Clone array to prevent accidental modification in the meantime
        char[] arrayCloned = array.clone();
        run(() -> out.writeSerialArray(arrayCloned));
        return this;
    }

    @Override
    public ArrayEnd elements(short[] array) {
        // Clone array to prevent accidental modification in the meantime
        short[] arrayCloned = array.clone();
        run(() -> out.writeSerialArray(arrayCloned));
        return this;
    }

    @Override
    public ArrayEnd elements(int[] array) {
        // Clone array to prevent accidental modification in the meantime
        int[] arrayCloned = array.clone();
        run(() -> out.writeSerialArray(arrayCloned));
        return this;
    }

    @Override
    public ArrayEnd elements(long[] array) {
        // Clone array to prevent accidental modification in the meantime
        long[] arrayCloned = array.clone();
        run(() -> out.writeSerialArray(arrayCloned));
        return this;
    }

    @Override
    public ArrayEnd elements(float[] array) {
        // Clone array to prevent accidental modification in the meantime
        float[] arrayCloned = array.clone();
        run(() -> out.writeSerialArray(arrayCloned));
        return this;
    }

    @Override
    public ArrayEnd elements(double[] array) {
        // Clone array to prevent accidental modification in the meantime
        double[] arrayCloned = array.clone();
        run(() -> out.writeSerialArray(arrayCloned));
        return this;
    }

    @Override
    public ArrayObjectElementsStart beginObjectElements() {
        pendingObjectsActions.addLast(new ArrayList<>());
        objectArrayElementCounts.addLast(new AtomicInteger(0));
        return this;
    }

    @Override
    public ArrayEnd endElements() {
        List<Runnable> actions = pendingObjectsActions.removeLast();
        int elementsCount = objectArrayElementCounts.removeLast().get();
        run(() -> {
            out.writeInt(elementsCount);
            actions.forEach(Runnable::run);
        });
        return this;
    }

    @Override
    public Object endArray() {
        nestingDepth--;
        run(pendingPostObjectActions.removeLast());

        // Remove the dummy count for the array itself (not its elements)
        AtomicInteger dummyElementCount = objectArrayElementCounts.removeLast();
        if (dummyElementCount != null) {
            throw new IllegalStateException("Unexpected element count: " + dummyElementCount);
        }

        return this;
    }

    @Override
    public DescriptorHierarchyStart<EnumStart<?>> beginEnum(Handle unassignedHandle) {
        Objects.requireNonNull(unassignedHandle);
        nestingDepth++;

        AtomicBoolean oldMode = new AtomicBoolean();
        run(() -> {
            boolean oldModeValue = out.setBlockDataMode(false);
            oldMode.set(oldModeValue);
            out.writeByte(TC_ENUM);
        });
        pendingPostObjectActions.addLast(() -> out.setBlockDataMode(oldMode.get()));
        onStartedObject(false);

        @SuppressWarnings("unchecked")
        DescriptorHierarchyStart<EnumStart<?>> result = createDescriptorHierarchyBuilder(unassignedHandle);
        return result;
    }

    @Override
    public EnumEnd name(String constantName) {
        Objects.requireNonNull(constantName);
        // Handle for string
        nextHandleIndex.getAndIncrement();
        run(() -> out.writeSerialString(constantName));
        return this;
    }

    @Override
    public Object endEnum() {
        nestingDepth--;
        run(pendingPostObjectActions.removeLast());
        return this;
    }

    @Override
    public DescriptorHierarchyStart beginClass(Handle unassignedHandle) {
        Objects.requireNonNull(unassignedHandle);
        nestingDepth++;

        AtomicBoolean oldMode = new AtomicBoolean();
        run(() -> {
            boolean oldModeValue = out.setBlockDataMode(false);
            oldMode.set(oldModeValue);
            run(() -> out.writeByte(TC_CLASS));
        });
        pendingPostObjectActions.addLast(() -> out.setBlockDataMode(oldMode.get()));
        onStartedObject(false);
        return createDescriptorHierarchyBuilder(unassignedHandle);
    }

    @Override
    public Object endClass() {
        nestingDepth--;
        run(pendingPostObjectActions.removeLast());
        return this;
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
    public DescriptorHierarchyStart<SerializableObjectStart<?>> beginSerializableObject(Handle unassignedHandle) {
        Objects.requireNonNull(unassignedHandle);
        nestingDepth++;

        AtomicBoolean oldMode = new AtomicBoolean();
        run(() -> {
            boolean oldModeValue = out.setBlockDataMode(false);
            oldMode.set(oldModeValue);
            out.writeByte(TC_OBJECT);
        });
        pendingPostObjectActions.addLast(() -> out.setBlockDataMode(oldMode.get()));
        onStartedObject(true);

        @SuppressWarnings("unchecked")
        DescriptorHierarchyStart<SerializableObjectStart<?>> result = createDescriptorHierarchyBuilder(unassignedHandle);
        return result;
    }

    @Override
    public Object externalizableObject(Handle unassignedHandle, Function writer) {
        @SuppressWarnings("unchecked")
        var returnedBuilder = writer.apply(beginExternalizableObject(unassignedHandle));
        if (returnedBuilder != this) {
            throw new IllegalStateException("Incorrect builder usage");
        }

        return this;
    }

    @Override
    public DescriptorHierarchyStart<ExternalizableObjectStart<?>> beginExternalizableObject(Handle unassignedHandle) {
        Objects.requireNonNull(unassignedHandle);
        nestingDepth++;

        AtomicBoolean oldMode = new AtomicBoolean();
        run(() -> {
            boolean oldModeValue = out.setBlockDataMode(false);
            oldMode.set(oldModeValue);
            out.writeByte(TC_OBJECT);
        });
        pendingPostObjectActions.addLast(() -> out.setBlockDataMode(oldMode.get()));
        // Use `canBeNested: true` (even though Externalizable cannot be nested with this builder)
        // to pop elements count in `endObject()`
        onStartedObject(true);

        @SuppressWarnings("unchecked")
        DescriptorHierarchyStart<ExternalizableObjectStart<?>> result = createDescriptorHierarchyBuilder(unassignedHandle);
        return result;
    }

    /**
     * Index of the current {@link ObjectBuildingDataOutput} scope. The value is increased every time a new
     * {@code ObjectBuildingDataOutput} is used, and decreased when the scope of the {@code ObjectBuildingDataOutput}
     * is left. This is used to detect accidental usage of the wrong {@code ObjectBuildingDataOutput} object in case
     * multiple are in scope (e.g. for nested Externalizable objects).
     */
    private int currentOutputScopeIndex = -1;

    private void writeDataWith(ThrowingConsumer<ObjectBuildingDataOutput> writer) {
        int originalNestingDepth = nestingDepth;
        int outputScopeIndex = ++currentOutputScopeIndex;

        ObjectBuildingDataOutput dataOutput = new ObjectBuildingDataOutput() {
            private void verifyOutputIsUsable() {
                if (outputScopeIndex != currentOutputScopeIndex) {
                    throw new IllegalStateException("Other output is currently active; make sure you called the method on the correct ObjectBuildingDataOutput variable");
                }
                if (nestingDepth != originalNestingDepth) {
                    throw new IllegalStateException("Previous builder call is incomplete; make sure all builder methods are called until the return type is Void");
                }
            }

            private void run(Runnable r) {
                verifyOutputIsUsable();
                SerialBuilderImpl.this.run(r);
            }

            @Override
            public void write(int b) {
                run(() -> out.write(b));
            }

            @Override
            public void write(byte[] b) {
                // Clone array to prevent accidental modification in the meantime
                var copy = b.clone();
                run(() -> out.write(copy));
            }

            @Override
            public void write(byte[] b, int off, int len) {
                Objects.checkFromIndexSize(off, len, b.length);
                // Clone array to prevent accidental modification in the meantime
                var copy = b.clone();
                run(() -> out.write(copy, off, len));
            }

            @Override
            public void writeBoolean(boolean v) {
                run(() -> out.writeBoolean(v));
            }

            @Override
            public void writeByte(int v) {
                run(() -> out.writeByte(v));
            }

            @Override
            public void writeShort(int v) {
                run(() -> out.writeShort(v));
            }

            @Override
            public void writeChar(int v) {
                run(() -> out.writeChar(v));
            }

            @Override
            public void writeInt(int v) {
                run(() -> out.writeInt(v));
            }

            @Override
            public void writeLong(long v) {
                run(() -> out.writeLong(v));
            }

            @Override
            public void writeFloat(float v) {
                run(() -> out.writeFloat(v));
            }

            @Override
            public void writeDouble(double v) {
                run(() -> out.writeDouble(v));
            }

            @Deprecated
            @Override
            public void writeBytes(String s) {
                Objects.requireNonNull(s);
                run(() -> out.writeBytes(s));
            }

            @Override
            public void writeChars(String s) {
                Objects.requireNonNull(s);
                run(() -> out.writeChars(s));
            }

            @Override
            public void writeUTF(String s) {
                Objects.requireNonNull(s);
                run(() -> out.writeUTF(s));
            }

            // ObjectStart methods

            @Override
            public Void objectHandle(Handle handle) {
                verifyOutputIsUsable();
                SerialBuilderImpl.this.objectHandle(handle);
                return null;
            }

            @Override
            public Void nullObject() {
                verifyOutputIsUsable();
                SerialBuilderImpl.this.nullObject();
                return null;
            }

            @Override
            public Void string(Handle unassignedHandle, String s) {
                verifyOutputIsUsable();
                SerialBuilderImpl.this.string(unassignedHandle, s);
                return null;
            }

            @SuppressWarnings("unchecked")
            @Override
            public DescriptorHierarchyStart<ArrayElements<Void>> beginArray(Handle unassignedHandle) {
                verifyOutputIsUsable();
                return (DescriptorHierarchyStart<ArrayElements<Void>>) (DescriptorHierarchyStart<?>) SerialBuilderImpl.this.beginArray(unassignedHandle);
            }

            @Override
            public Void array(Handle unassignedHandle, Function<DescriptorHierarchyStart<ArrayElements<Enclosing>>, Enclosing> writer) {
                verifyOutputIsUsable();
                SerialBuilderImpl.this.array(unassignedHandle, writer);
                return null;
            }

            @SuppressWarnings("unchecked")
            @Override
            public DescriptorHierarchyStart<EnumStart<Void>> beginEnum(Handle unassignedHandle) {
                verifyOutputIsUsable();
                return (DescriptorHierarchyStart<EnumStart<Void>>) (DescriptorHierarchyStart<?>) SerialBuilderImpl.this.beginEnum(unassignedHandle);
            }

            @SuppressWarnings("unchecked")
            @Override
            public DescriptorHierarchyStart<ClassEnd<Void>> beginClass(Handle unassignedHandle) {
                verifyOutputIsUsable();
                return SerialBuilderImpl.this.beginClass(unassignedHandle);
            }

            @SuppressWarnings("unchecked")
            @Override
            public DescriptorHierarchyStart<SerializableObjectStart<Void>> beginSerializableObject(Handle unassignedHandle) {
                verifyOutputIsUsable();
                return (DescriptorHierarchyStart<SerializableObjectStart<Void>>) (DescriptorHierarchyStart<?>) SerialBuilderImpl.this.beginSerializableObject(unassignedHandle);
            }

            @Override
            public Void serializableObject(Handle unassignedHandle, Function<DescriptorHierarchyStart<SerializableObjectStart<Enclosing>>, Enclosing> writer) {
                verifyOutputIsUsable();
                SerialBuilderImpl.this.serializableObject(unassignedHandle, writer);
                return null;
            }

            @SuppressWarnings("unchecked")
            @Override
            public DescriptorHierarchyStart<ExternalizableObjectStart<Void>> beginExternalizableObject(Handle unassignedHandle) {
                verifyOutputIsUsable();
                return (DescriptorHierarchyStart<ExternalizableObjectStart<Void>>) (DescriptorHierarchyStart<?>) SerialBuilderImpl.this.beginExternalizableObject(unassignedHandle);
            }

            @Override
            public Void externalizableObject(Handle unassignedHandle, Function<DescriptorHierarchyStart<ExternalizableObjectStart<Enclosing>>, Enclosing> writer) {
                verifyOutputIsUsable();
                SerialBuilderImpl.this.externalizableObject(unassignedHandle, writer);
                return null;
            }
        };
        try {
            writer.accept(dataOutput);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        if (nestingDepth != originalNestingDepth) {
            throw new IllegalStateException("Usage of ObjectBuildingDataOutput did not complete builder call; make sure all builder methods are called until the return type is Void");
        }
        currentOutputScopeIndex--;
    }

    @Override
    public ObjectEnd writeExternalWith(ThrowingConsumer writer) {
        boolean writeBlockData = protocolVersion.compareTo(ProtocolVersion.V2) >= 0;

        if (writeBlockData) {
            run(() -> out.setBlockDataMode(true));
        }

        @SuppressWarnings("unchecked")
        ThrowingConsumer<ObjectBuildingDataOutput> writerT = writer;
        writeDataWith(writerT);

        if (writeBlockData) {
            run(() -> out.setBlockDataMode(false));
            run(() -> out.writeByte(TC_ENDBLOCKDATA));
        }
        return this;
    }

    @Override
    public SlotsStart beginSlots() {
        return this;
    }

    private final Deque<AtomicBoolean> hasWrittenSlot = new LinkedList<>();
    private final Deque<Queue<Runnable>> fieldActions = new LinkedList<>();

    @Override
    public SlotStart beginSlot() {
        hasWrittenSlot.addLast(new AtomicBoolean(false));
        fieldActions.addLast(new LinkedList<>());
        return this;
    }

    private List<ThrowingConsumer<DataOutput>> primitiveFieldsActions;

    @Override
    public SlotPrimitiveFields beginPrimitiveFields() {
        primitiveFieldsActions = new ArrayList<>();
        return this;
    }

    @Override
    public SlotPrimitiveFields booleanValue(boolean b) {
        primitiveFieldsActions.add(dataOut -> dataOut.writeBoolean(b));
        return this;
    }

    @Override
    public SlotPrimitiveFields byteValue(byte b) {
        primitiveFieldsActions.add(dataOut -> dataOut.writeByte(b));
        return this;
    }

    @Override
    public SlotPrimitiveFields charValue(char c) {
        primitiveFieldsActions.add(dataOut -> dataOut.writeChar(c));
        return this;
    }

    @Override
    public SlotPrimitiveFields shortValue(short s) {
        primitiveFieldsActions.add(dataOut -> dataOut.writeShort(s));
        return this;
    }

    @Override
    public SlotPrimitiveFields intValue(int i) {
        primitiveFieldsActions.add(dataOut -> dataOut.writeInt(i));
        return this;
    }

    @Override
    public SlotPrimitiveFields longValue(long l) {
        primitiveFieldsActions.add(dataOut -> dataOut.writeLong(l));
        return this;
    }

    @Override
    public SlotPrimitiveFields floatValue(float f) {
        primitiveFieldsActions.add(dataOut -> dataOut.writeFloat(f));
        return this;
    }

    @Override
    public SlotPrimitiveFields doubleValue(double d) {
        primitiveFieldsActions.add(dataOut -> dataOut.writeDouble(d));
        return this;
    }

    @Override
    public SlotObjectFieldsStart endPrimitiveFields() {
        ByteArrayOutputStream tempOut = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(tempOut);
        primitiveFieldsActions.forEach(action -> {
            try {
                action.accept(dataOut);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });

        try {
            dataOut.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        byte[] array = tempOut.toByteArray();
        primitiveFieldsActions = null;
        run(() -> out.write(array));

        return this;
    }

    @Override
    public SlotObjectFields beginObjectFields() {
        return this;
    }

    @Override
    public SlotWriteObjectMethodData endObjectFields() {
        return this;
    }

    @Override
    public SlotEnd writeObjectWith(ThrowingConsumer writer) {
        hasWrittenSlot.getLast().set(true);

        run(() -> out.setBlockDataMode(true));

        @SuppressWarnings("unchecked")
        ThrowingConsumer<ObjectBuildingDataOutput> writerT = writer;
        writeDataWith(writerT);

        // Write the fields written with `defaultWriteObject()`, if any
        Queue<Runnable> fieldActions = this.fieldActions.removeLast();
        if (!fieldActions.isEmpty()) {
            run(() -> out.setBlockDataMode(false));
            fieldActions.forEach(this::run);
            run(() -> out.setBlockDataMode(true));
        }

        run(() -> {
            out.setBlockDataMode(false);
            out.writeByte(TC_ENDBLOCKDATA);
        });
        return this;
    }

    @Override
    public SlotsStart endSlot() {
        if (!hasWrittenSlot.removeLast().get()) {
            Queue<Runnable> fieldActions = this.fieldActions.removeLast();
            fieldActions.forEach(this::run);
        }
        return this;
    }

    @Override
    public ObjectEnd endSlots() {
        return this;
    }

    private byte[] getSerialData() {
        out.close();
        return binaryOut.toByteArray();
    }

    @Override
    public Object endObject() {
        nestingDepth--;
        run(pendingPostObjectActions.removeLast());

        AtomicInteger dummyElementCount = objectArrayElementCounts.removeLast();
        // When not currently writing object array elements, element count is dummy `null`
        if (dummyElementCount != null) {
            throw new IllegalStateException("Unexpected element count: " + dummyElementCount);
        }

        // Check if top-level object was finished
        if (nestingDepth == 0) {
            if (!objectArrayElementCounts.isEmpty()) {
                throw new AssertionError("Unprocessed element counts: " + objectArrayElementCounts);
            }
            if (!pendingPostObjectActions.isEmpty()) {
                throw new AssertionError("Unprocessed post object actions: " + pendingPostObjectActions.size());
            }

            if (isBuildingSingleObject) {
                return getSerialData();
            } else {
                return null;
            }
        } else {
            return this;
        }
    }

    @SuppressWarnings("unchecked")
    public static ObjectStart<byte[]> createStart() {
        return new SerialBuilderImpl(true);
    }

    public static SerialBuilder.SerializableBuilderStart startSerializable(Handle unassignedHandle) {
        var delegate = createStart().beginSerializableObject(unassignedHandle);
        return new SerialBuilder.SerializableBuilderStart() {
            @Override
            public DescriptorsList<SerializableObjectStart<byte[]>> beginDescriptorHierarchy() {
                return delegate.beginDescriptorHierarchy();
            }

            @Override
            public SerializableObjectStart<byte[]> descriptorHierarchy(Function<DescriptorsList<Enclosing>, Enclosing> writer) {
                return delegate.descriptorHierarchy(writer);
            }
        };
    }

    public static SerialBuilder.ExternalizableBuilderStart startExternalizable(Handle unassignedHandle) {
        var delegate = createStart().beginExternalizableObject(unassignedHandle);
        return new SerialBuilder.ExternalizableBuilderStart() {
            @Override
            public DescriptorsList<ExternalizableObjectStart<byte[]>> beginDescriptorHierarchy() {
                return delegate.beginDescriptorHierarchy();
            }

            @Override
            public ExternalizableObjectStart<byte[]> descriptorHierarchy(Function<DescriptorsList<Enclosing>, Enclosing> writer) {
                return delegate.descriptorHierarchy(writer);
            }
        };
    }

    public static byte[] writeSerializationDataWith(ThrowingConsumer<ObjectBuildingDataOutput> writer) {
        SerialBuilderImpl serialBuilder = new SerialBuilderImpl(false);
        serialBuilder.out.setBlockDataMode(true);
        serialBuilder.writeDataWith(writer);
        serialBuilder.out.setBlockDataMode(false);
        return serialBuilder.getSerialData();
    }
}
