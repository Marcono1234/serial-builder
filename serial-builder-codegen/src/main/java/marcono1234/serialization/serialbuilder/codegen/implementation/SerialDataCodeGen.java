package marcono1234.serialization.serialbuilder.codegen.implementation;

import marcono1234.serialization.serialbuilder.codegen.implementation.streamdata.ClassObject;
import marcono1234.serialization.serialbuilder.codegen.implementation.streamdata.EnumConstantObject;
import marcono1234.serialization.serialbuilder.codegen.implementation.streamdata.ExternalizableObject;
import marcono1234.serialization.serialbuilder.codegen.implementation.streamdata.HandleAssignableObject;
import marcono1234.serialization.serialbuilder.codegen.implementation.streamdata.HandleObject;
import marcono1234.serialization.serialbuilder.codegen.implementation.streamdata.NullObject;
import marcono1234.serialization.serialbuilder.codegen.implementation.streamdata.ObjectAnnotationContent;
import marcono1234.serialization.serialbuilder.codegen.implementation.streamdata.ProxyObject;
import marcono1234.serialization.serialbuilder.codegen.implementation.streamdata.SerializableObject;
import marcono1234.serialization.serialbuilder.codegen.implementation.streamdata.SerializableObject.SerializableClassData.ObjectFieldValue;
import marcono1234.serialization.serialbuilder.codegen.implementation.streamdata.SerializableObject.SerializableClassData.PrimitiveFieldValue;
import marcono1234.serialization.serialbuilder.codegen.implementation.streamdata.StreamObject;
import marcono1234.serialization.serialbuilder.codegen.implementation.streamdata.StringObject;
import marcono1234.serialization.serialbuilder.codegen.implementation.streamdata.WrappedUnassignableObject;
import marcono1234.serialization.serialbuilder.codegen.implementation.streamdata.WritableStreamObject;
import marcono1234.serialization.serialbuilder.codegen.implementation.streamdata.array.ArrayObject;
import marcono1234.serialization.serialbuilder.codegen.implementation.streamdata.array.ObjectArrayObject;
import marcono1234.serialization.serialbuilder.codegen.implementation.streamdata.array.PrimitiveArrayObject;
import marcono1234.serialization.serialbuilder.codegen.implementation.streamdata.descriptor.DescriptorStreamObject;
import marcono1234.serialization.serialbuilder.codegen.implementation.streamdata.descriptor.NonProxyDescriptorData;
import marcono1234.serialization.serialbuilder.codegen.implementation.streamdata.descriptor.ObjectField;
import marcono1234.serialization.serialbuilder.codegen.implementation.streamdata.descriptor.PrimitiveField;
import marcono1234.serialization.serialbuilder.codegen.implementation.streamdata.descriptor.PrimitiveField.PrimitiveFieldType;
import marcono1234.serialization.serialbuilder.codegen.implementation.streamdata.descriptor.ProxyDescriptorData;
import marcono1234.serialization.serialbuilder.codegen.implementation.writer.CodeWriter;
import marcono1234.serialization.serialbuilder.codegen.implementation.writer.TopLevelCodeWritable;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutput;
import java.io.StreamCorruptedException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.io.ObjectStreamConstants.SC_BLOCK_DATA;
import static java.io.ObjectStreamConstants.SC_ENUM;
import static java.io.ObjectStreamConstants.SC_EXTERNALIZABLE;
import static java.io.ObjectStreamConstants.SC_SERIALIZABLE;
import static java.io.ObjectStreamConstants.SC_WRITE_METHOD;
import static java.io.ObjectStreamConstants.STREAM_MAGIC;
import static java.io.ObjectStreamConstants.STREAM_VERSION;
import static java.io.ObjectStreamConstants.TC_ARRAY;
import static java.io.ObjectStreamConstants.TC_BLOCKDATA;
import static java.io.ObjectStreamConstants.TC_BLOCKDATALONG;
import static java.io.ObjectStreamConstants.TC_CLASS;
import static java.io.ObjectStreamConstants.TC_CLASSDESC;
import static java.io.ObjectStreamConstants.TC_ENDBLOCKDATA;
import static java.io.ObjectStreamConstants.TC_ENUM;
import static java.io.ObjectStreamConstants.TC_EXCEPTION;
import static java.io.ObjectStreamConstants.TC_LONGSTRING;
import static java.io.ObjectStreamConstants.TC_NULL;
import static java.io.ObjectStreamConstants.TC_OBJECT;
import static java.io.ObjectStreamConstants.TC_PROXYCLASSDESC;
import static java.io.ObjectStreamConstants.TC_REFERENCE;
import static java.io.ObjectStreamConstants.TC_RESET;
import static java.io.ObjectStreamConstants.TC_STRING;
import static java.io.ObjectStreamConstants.baseWireHandle;

// https://docs.oracle.com/en/java/javase/17/docs/specs/serialization/protocol.html#grammar-for-the-stream-format
public class SerialDataCodeGen implements Closeable {
    /**
     * Thrown for serialization data which is (probably) allowed by the grammar,
     * but cannot be recreated using the builder API.
     *
     * <p>For cases where it is possible to create at least very similar serialization
     * data and where the differences should not matter, no exception should be thrown
     * and a {@linkplain CodeWriter#writeComment(String) comment} should be written instead.
     */
    static class UnsupportedStreamFeatureException extends IOException {
        public UnsupportedStreamFeatureException(String streamFeature) {
            super("Unsupported feature: " + streamFeature);
        }
    }

    private static final HexFormat hexFormat = HexFormat.of().withPrefix("0x");

    public static final String GENERATED_SERIAL_DATA_VARIABLE = "byte[] serialData = ";

    private final DataInputStream dataIn;
    private final HandleManager handleManager;

    public SerialDataCodeGen(InputStream in) {
        dataIn = new DataInputStream(Objects.requireNonNull(in));
        handleManager = new HandleManager();
    }

    public String generateCode(boolean writeComments, boolean writeUnsupportedHandleComments) throws IOException {
        short magic = dataIn.readShort();
        if (magic != STREAM_MAGIC) {
            throw new StreamCorruptedException("Invalid stream magic");
        }
        short version = dataIn.readShort();
        if (version != STREAM_VERSION) {
            throw new StreamCorruptedException("Invalid stream version");
        }

        CodeWriter codeWriter = new CodeWriter(writeComments, writeUnsupportedHandleComments);
        VariableNameManager variableNameManager = new VariableNameManager();

        List<ObjectAnnotationContent> contents = readContents(true);
        List<String> usedHandleNames = handleManager.getUsedHandleNames();
        for (String handleName : usedHandleNames) {
            codeWriter.writeLine("Handle " + handleName + " = new Handle();");
            variableNameManager.markNameUsed(handleName);
        }
        if (!usedHandleNames.isEmpty()) {
            // Write empty line
            codeWriter.writeLine("");
        }

        // If only one top level writable, let it write its specialized serial-builder method
        if (contents.size() == 1 && contents.get(0) instanceof ObjectAnnotationContent.ObjectObjectAnnotationContent objectContent && objectContent.object() instanceof TopLevelCodeWritable topLevelWritable) {
            topLevelWritable.writeTopLevelCode(codeWriter, handleManager, variableNameManager);
        } else {
            String writerVarName = "writer";
            variableNameManager.markNameUsed(writerVarName);
            codeWriter.writeLine(GENERATED_SERIAL_DATA_VARIABLE + "SimpleSerialBuilder.writeSerializationDataWith(" + writerVarName + " -> {");
            codeWriter.increaseIndentation();

            if (contents.isEmpty()) {
                codeWriter.writeComment("Empty serialization stream");
            } else {
                contents.forEach(content -> content.writeCode(codeWriter, handleManager, variableNameManager, writerVarName));
            }

            codeWriter.decreaseIndentation();
            codeWriter.writeLine("});");
        }

        return codeWriter.getCode();
    }

    private WritableStreamObject readObject() throws IOException {
        return readObject(dataIn.readByte());
    }

    private WritableStreamObject readObject(byte type) throws IOException {
        return switch (type) {
            case TC_OBJECT -> readNewObject();
            case TC_CLASS -> readNewClass();
            case TC_ARRAY -> readNewArray();
            case TC_STRING, TC_LONGSTRING -> readNewString(type);
            case TC_ENUM -> readNewEnum();
            case TC_CLASSDESC, TC_PROXYCLASSDESC -> throw new UnsupportedStreamFeatureException("Class descriptor as object (ObjectStreamClass)");
            case TC_REFERENCE -> {
                var handle = readPrevObject();
                Class<? extends StreamObject> referencedObjectType = handle.getObjectType();
                if (HandleAssignableObject.class.isAssignableFrom(referencedObjectType)) {
                    @SuppressWarnings("unchecked")
                    var handleT = (HandleManager.Handle<? extends HandleAssignableObject>) handle;
                    yield new HandleObject(handleT);
                }

                // Cover objects for which builder API does not support referencing handle
                String typeDescription;
                if (StringObject.class.isAssignableFrom(referencedObjectType)) {
                    typeDescription = "String object";
                } else if (EnumConstantObject.class.isAssignableFrom(referencedObjectType)) {
                    typeDescription = "Enum constant";
                } else if (ClassObject.class.isAssignableFrom(referencedObjectType)) {
                    typeDescription = "Class object";
                } else {
                    throw new UnsupportedStreamFeatureException("Unsupported object type as handle value: " + referencedObjectType);
                }
                WritableStreamObject referencedObject = (WritableStreamObject) handle.getReferencedObject().orElseThrow(() -> new AssertionError(typeDescription + " should already be assigned"));
                yield new WrappedUnassignableObject(referencedObject, typeDescription);
            }
            case TC_NULL -> NullObject.INSTANCE;
            case TC_EXCEPTION -> throw new UnsupportedStreamFeatureException("TC_EXCEPTION");
            case TC_RESET -> throw new UnsupportedStreamFeatureException("TC_RESET");
            default -> throw new StreamCorruptedException("Unknown type: " + hexFormat.toHexDigits(type));
        };
    }

    private WritableStreamObject readNewObject() throws IOException {
        ReadDescriptor<? extends DescriptorStreamObject> readClassDesc = readClassDesc().orElseThrow(() -> new UnsupportedStreamFeatureException("Null class descriptor"));
        DescriptorStreamObject classDesc = readClassDesc.descriptor();

        if (classDesc instanceof ProxyDescriptorData proxyClassDesc) {
            var handle = handleManager.newUnassignedHandle(ProxyObject.class);

            Optional<NonProxyDescriptorData> optProxySuperClass = proxyClassDesc.superClassDesc();
            if (optProxySuperClass.isEmpty()) {
                throw new UnsupportedStreamFeatureException("Proxy without super class descriptor");
            }
            NonProxyDescriptorData proxySuperClass = optProxySuperClass.get();

            validateWellformedProxySuperClass(proxySuperClass);
            WritableStreamObject invocationHandler = readObject();

            // Note: Could also validate object referenced by handle, but builder API supports mismatching
            // type at the moment (e.g. array object as invocation handler), therefore permit this for now
            if (!(invocationHandler instanceof HandleObject || invocationHandler instanceof ProxyObject || invocationHandler instanceof SerializableObject || invocationHandler instanceof ExternalizableObject)) {
                throw new UnsupportedStreamFeatureException("Proxy invocation handler " + invocationHandler);
            }

            boolean usesClassDescHandle = proxyClassDesc.usesAnyHandle();
            ProxyObject proxyObject = new ProxyObject(proxyClassDesc.interfaceNames(), invocationHandler, usesClassDescHandle, handle);
            handle.setObject(proxyObject);
            return proxyObject;
        } else if (classDesc instanceof NonProxyDescriptorData nonProxyClassDesc) {
            byte flags = nonProxyClassDesc.flags();

            if ((flags & SC_EXTERNALIZABLE) != 0) {
                if ((flags & SC_BLOCK_DATA) == 0) {
                    // Externalizable data not in block data form cannot be read because it does not have any
                    // indication where the data ends
                    throw new UnsupportedStreamFeatureException("Externalizable without block data");
                }

                var handle = handleManager.newUnassignedHandle(ExternalizableObject.class);

                String typeName = nonProxyClassDesc.typeName();
                long serialVersionUid = nonProxyClassDesc.serialVersionUid();

                boolean hasFields = !(nonProxyClassDesc.primitiveFields().isEmpty() && nonProxyClassDesc.objectFields().isEmpty());
                boolean hasSuperClass = nonProxyClassDesc.superClassDesc().isPresent();

                ExternalizableObject externalizableObject = new ExternalizableObject(
                    typeName,
                    serialVersionUid,
                    readObjectAnnotationData(),
                    readClassDesc.isHandle(),
                    hasFields,
                    hasSuperClass,
                    handle
                );
                handle.setObject(externalizableObject);
                return externalizableObject;
            } else {
                var handle = handleManager.newUnassignedHandle(SerializableObject.class);
                SerializableObject serializableObject = readSerializableObject(nonProxyClassDesc, readClassDesc.isHandle(), handle);
                handle.setObject(serializableObject);
                return serializableObject;
            }
        } else {
            // All possible DescriptorStreamObject subtypes have been covered above
            throw new AssertionError("Unexpected class descriptor type: " + classDesc);
        }
    }

    /**
     * Validate that super class of proxy is non-proxy descriptor for {@link java.lang.reflect.Proxy}.
     */
    private static void validateWellformedProxySuperClass(NonProxyDescriptorData proxySuperClass) throws UnsupportedStreamFeatureException {
        String superClassName = proxySuperClass.typeName();
        if (!superClassName.equals("java.lang.reflect.Proxy")) {
            throw new UnsupportedStreamFeatureException("Proxy super class " + superClassName);
        }

        long serialVersionUid = proxySuperClass.serialVersionUid();
        if (serialVersionUid != -2222568056686623797L) {
            throw new UnsupportedStreamFeatureException("Proxy serial version UID " + serialVersionUid);
        }

        byte flags = proxySuperClass.flags();
        if (flags != SC_SERIALIZABLE) {
            throw new UnsupportedStreamFeatureException("Proxy super class flags " + flags);
        }

        List<PrimitiveField> primitiveFields = proxySuperClass.primitiveFields();
        if (!primitiveFields.isEmpty()) {
            throw new UnsupportedStreamFeatureException("Proxy super class primitive fields " + primitiveFields);
        }

        List<ObjectField> objectFields = proxySuperClass.objectFields();
        boolean hasValidObjectFields = objectFields.size() == 1;
        if (hasValidObjectFields) {
            ObjectField handleField = objectFields.get(0);
            hasValidObjectFields = handleField.name().equals("h") && handleField.typeName().equals("java.lang.reflect.InvocationHandler");
        }

        if (!hasValidObjectFields) {
            throw new UnsupportedStreamFeatureException("Proxy super class object fields " + objectFields);
        }

        Optional<NonProxyDescriptorData> optSuperSuperClass = proxySuperClass.superClassDesc();
        if (optSuperSuperClass.isPresent()) {
            throw new UnsupportedStreamFeatureException("Proxy super super class " + optSuperSuperClass);
        }
    }

    private SerializableObject readSerializableObject(NonProxyDescriptorData classDesc, boolean isClassDescHandle, HandleManager.Handle<SerializableObject> ownHandle) throws IOException {
        List<NonProxyDescriptorData> descriptors = getDescriptorHierarchy(classDesc);
        List<SerializableObject.SerializableClassData> classDataList = new ArrayList<>();

        for (int i = 0; i < descriptors.size(); i++) {
            NonProxyDescriptorData currentDesc = descriptors.get(i);
            String typeName = currentDesc.typeName();
            long serialVersionUid = currentDesc.serialVersionUid();
            byte flags = currentDesc.flags();

            boolean usesDescriptorHandle;
            if (i < descriptors.size() - 1) {
                // Check for subtype whether it used handle for `currentDesc`
                usesDescriptorHandle = descriptors.get(i + 1).superClassDescUsesHandle();
            } else {
                assert i == descriptors.size() - 1;
                usesDescriptorHandle = isClassDescHandle;
            }

            if (flags == SC_SERIALIZABLE || flags == (SC_SERIALIZABLE | SC_WRITE_METHOD)) {
                List<PrimitiveField> primitiveFields = currentDesc.primitiveFields();
                int primitiveDataLength = primitiveFields.stream().mapToInt(f -> f.type().getValueBytesCount()).sum();
                byte[] primitiveData = dataIn.readNBytes(primitiveDataLength);
                DataInputStream primitiveDataIn = new DataInputStream(new ByteArrayInputStream(primitiveData));

                List<PrimitiveFieldValue> primitiveFieldValues = new ArrayList<>();
                for (PrimitiveField field : primitiveFields) {
                    Object value = field.type().readValue(primitiveDataIn);
                    primitiveFieldValues.add(new PrimitiveFieldValue(field.name(), value));
                }

                assert primitiveDataIn.read() == -1;

                List<ObjectFieldValue> objectFieldValues = new ArrayList<>();
                for (ObjectField field : currentDesc.objectFields()) {
                    objectFieldValues.add(new ObjectFieldValue(field.name(), field.typeName(), readObject(), field.usesTypeNameHandle()));
                }

                Optional<List<ObjectAnnotationContent>> objectAnnotationData;
                if ((flags & SC_WRITE_METHOD) != 0) {
                    objectAnnotationData = Optional.of(readObjectAnnotationData());
                } else {
                    objectAnnotationData = Optional.empty();
                }

                classDataList.add(new SerializableObject.SerializableClassData(
                    typeName,
                    serialVersionUid,
                    primitiveFieldValues,
                    objectFieldValues,
                    objectAnnotationData,
                    usesDescriptorHandle
                ));
            } else if ((flags & SC_EXTERNALIZABLE) != 0) {
                // If any of the types is Externalizable, then all subtypes should be Externalizable as well;
                // Externalizable types are handled by the caller of this method, therefore if Externalizable
                // appears within hierarchy it is not well-formed
                throw new UnsupportedStreamFeatureException("Externalizable class in Serializable hierarchy: " + currentDesc);
            } else {
                throw new UnsupportedStreamFeatureException("Flags " + flags);
            }
        }
        return new SerializableObject(classDataList, ownHandle);
    }

    private static List<NonProxyDescriptorData> getDescriptorHierarchy(NonProxyDescriptorData currentDesc) {
        LinkedList<NonProxyDescriptorData> classDescriptors = new LinkedList<>();
        while (currentDesc != null) {
            // Class descriptors are in reverse order (subtype to supertype)
            classDescriptors.addFirst(currentDesc);
            currentDesc = currentDesc.superClassDesc().orElse(null);
        }
        return classDescriptors;
    }

    /**
     * Reads the data written by {@link java.io.Externalizable#writeExternal(ObjectOutput)} or by {@code writeObject}.
     */
    private List<ObjectAnnotationContent> readObjectAnnotationData() throws IOException {
        return readContents(false);
    }

    private List<ObjectAnnotationContent> readContents(boolean isTopLevel) throws IOException {
        List<ObjectAnnotationContent> data = new ArrayList<>();
        while (true) {
            int b = dataIn.read();
            int blockLength;

            if (b == TC_BLOCKDATA) {
                blockLength = dataIn.readUnsignedByte();
            } else if (b == TC_BLOCKDATALONG) {
                blockLength = dataIn.readInt();
                if (blockLength < 0) {
                    throw new StreamCorruptedException("Invalid block length: " + blockLength);
                }
            } else if (b == TC_ENDBLOCKDATA) {
                break;
            } else if (b == -1) {
                if (isTopLevel) {
                    break;
                }
                throw new EOFException("Reached end of file while reading object annotation data");
            } else {
                data.add(new ObjectAnnotationContent.ObjectObjectAnnotationContent(readObject((byte) b)));
                continue;
            }

            byte[] bytes = dataIn.readNBytes(blockLength);
            data.add(new ObjectAnnotationContent.BinaryObjectAnnotationContent(bytes));
        }

        return data;
    }

    private ClassObject readNewClass() throws IOException {
        ReadDescriptor<?> readDescriptor = readClassDesc().orElseThrow(() -> new UnsupportedStreamFeatureException("Null class reference"));
        ClassObject classObject = new ClassObject(readDescriptor.descriptor(), readDescriptor.isHandle);
        handleManager.createAssignedHandle(classObject);
        return classObject;
    }

    private ReadDescriptor<NonProxyDescriptorData> readNonNullNonProxyClassDesc() throws IOException {
        return readNonProxyClassDesc().orElseThrow(() -> new UnsupportedStreamFeatureException("Null class descriptor"));
    }

    private Optional<ReadDescriptor<NonProxyDescriptorData>> readNonProxyClassDesc() throws IOException {
        var optClassDesc = readClassDesc();
        if (optClassDesc.isPresent()) {
            ReadDescriptor<?> readDescriptor = optClassDesc.get();
            DescriptorStreamObject classDesc = readDescriptor.descriptor();
            if (classDesc instanceof NonProxyDescriptorData nonProxyClassDesc) {
                return Optional.of(new ReadDescriptor<>(nonProxyClassDesc, readDescriptor.isHandle));
            }
            throw new UnsupportedStreamFeatureException("Class desc other than non-proxy: " + classDesc);
        }
        return Optional.empty();
    }

    private record ReadDescriptor<T extends DescriptorStreamObject>(T descriptor, boolean isHandle) {
    }

    private Optional<ReadDescriptor<?>> readClassDesc() throws IOException {
        byte type = dataIn.readByte();
        return switch (type) {
            case TC_CLASSDESC, TC_PROXYCLASSDESC -> Optional.of(new ReadDescriptor<>(readNewClassDesc(type), false));
            case TC_NULL -> Optional.empty();
            case TC_REFERENCE -> {
                // Require that handle is already assigned because circular references are not supported
                StreamObject referencedObject = readPrevAssignedObject();
                if (referencedObject instanceof DescriptorStreamObject descriptorStreamObject) {
                    yield Optional.of(new ReadDescriptor<>(descriptorStreamObject, true));
                }
                throw new UnsupportedStreamFeatureException("Reference to non-descriptor " + referencedObject);
            }
            default -> throw new StreamCorruptedException("Unknown type: " + hexFormat.toHexDigits(type));
        };
    }

    private ArrayObject readNewArray() throws IOException {
        ReadDescriptor<NonProxyDescriptorData> readClassDesc = readNonNullNonProxyClassDesc();
        NonProxyDescriptorData classDesc = readClassDesc.descriptor();
        validateArrayClassDesc(classDesc);

        var handle = handleManager.newUnassignedHandle(ArrayObject.class);

        int length = dataIn.readInt();
        if (length < 0) {
            throw new StreamCorruptedException("Invalid array length: " + length);
        }

        boolean usesDescriptorHandle = readClassDesc.isHandle() || classDesc.usesAnyHandle();

        String arrayTypeName = TypeNameHelper.createClassTypeName(classDesc.typeName(), false);
        ArrayObject arrayObject = switch (arrayTypeName) {
            case "byte[]" -> readPrimitiveArray(byte.class, DataInput::readByte, length, usesDescriptorHandle, handle);
            case "char[]" -> readPrimitiveArray(char.class, DataInput::readChar, length, usesDescriptorHandle, handle);
            case "double[]" -> readPrimitiveArray(double.class, DataInput::readDouble, length, usesDescriptorHandle, handle);
            case "float[]" -> readPrimitiveArray(float.class, DataInput::readFloat, length, usesDescriptorHandle, handle);
            case "int[]" -> readPrimitiveArray(int.class, DataInput::readInt, length, usesDescriptorHandle, handle);
            case "long[]" -> readPrimitiveArray(long.class, DataInput::readLong, length, usesDescriptorHandle, handle);
            case "short[]" -> readPrimitiveArray(short.class, DataInput::readShort, length, usesDescriptorHandle, handle);
            case "boolean[]" -> readPrimitiveArray(boolean.class, DataInput::readBoolean, length, usesDescriptorHandle, handle);
            default -> {
                if (!arrayTypeName.endsWith("[]")) {
                    throw new UnsupportedStreamFeatureException("Array type which is not an array: " + arrayTypeName);
                }
                // Note: Don't pre-size list or array with `length` because it might not come from trusted source
                List<WritableStreamObject> elements = new ArrayList<>();

                for (int i = 0; i < length; i++) {
                    elements.add(readObject());
                }
                yield new ObjectArrayObject(arrayTypeName, elements, usesDescriptorHandle, handle);
            }
        };
        handle.setObject(arrayObject);
        return arrayObject;
    }

    @FunctionalInterface
    private interface ReaderFunction {
        Object readValue(DataInput in) throws IOException;
    }

    private PrimitiveArrayObject readPrimitiveArray(Class<?> elementType, ReaderFunction readerFunction, int length, boolean usesDescriptorHandle, HandleManager.Handle<ArrayObject> ownHandle) throws IOException {
        assert elementType.isPrimitive();

        // Note: Don't pre-size list or array with `length` because it might not come from trusted source
        List<Object> values = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            values.add(readerFunction.readValue(dataIn));
        }

        Object array = Array.newInstance(elementType, length);
        for (int i = 0; i < length; i++) {
            Array.set(array, i, values.get(i));
        }
        return new PrimitiveArrayObject(array, usesDescriptorHandle, ownHandle);
    }

    private static void validateArrayClassDesc(NonProxyDescriptorData classDesc) throws UnsupportedStreamFeatureException {
        String typeName = classDesc.typeName();
        if (!typeName.endsWith("[]")) {
            throw new UnsupportedStreamFeatureException("Non array type " + typeName);
        }

        // Ignore serial version UID because specification says it is ignored for arrays

        byte flags = classDesc.flags();
        if (flags != SC_SERIALIZABLE) {
            throw new UnsupportedStreamFeatureException("Array descriptor flags " + flags);
        }

        List<PrimitiveField> primitiveFields = classDesc.primitiveFields();
        if (!primitiveFields.isEmpty()) {
            throw new UnsupportedStreamFeatureException("Array descriptor with primitive fields " + primitiveFields);
        }

        List<ObjectField> objectFields = classDesc.objectFields();
        if (!objectFields.isEmpty()) {
            throw new UnsupportedStreamFeatureException("Array descriptor with object fields " + objectFields);
        }

        Optional<NonProxyDescriptorData> optSuperClassDesc = classDesc.superClassDesc();
        if (optSuperClassDesc.isPresent()) {
            throw new UnsupportedStreamFeatureException("Array super class " + optSuperClassDesc);
        }
    }

    private StringObject readNewString(byte type) throws IOException {
        long length = switch (type) {
            case TC_STRING -> dataIn.readUnsignedShort();
            case TC_LONGSTRING -> dataIn.readLong();
            default -> throw new StreamCorruptedException("Unknown string type: " + hexFormat.toHexDigits(type));
        };

        if (length < 0) {
            throw new StreamCorruptedException("Invalid string length: " + length);
        }

        long remainingBytes = length;
        StringBuilder stringBuilder = new StringBuilder();

        while (remainingBytes > 0) {
            // https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/io/DataInput.html#modified-utf-8

            char c;
            byte first = dataIn.readByte();
            if ((0b1000_0000 & first) == 0) {
                c = (char) first;
                if (c == 0x0000) {
                    throw new StreamCorruptedException("Char out of valid range");
                }
                remainingBytes--;
            } else if ((0b1110_0000 & first) == 0b1100_0000) {
                if (remainingBytes < 2) {
                    throw new StreamCorruptedException("Incomplete two byte char");
                }

                byte second = dataIn.readByte();
                if ((0b1100_0000 & second) != 0b1000_0000) {
                    throw new StreamCorruptedException("Malformed string");
                }
                c = (char) (((0b0001_1111 & first) << 6) | (0b0011_1111 & second));
                if (!(c == 0x0000 || c >= 0x0080)) {
                    throw new StreamCorruptedException("Char out of valid range");
                }
                remainingBytes -= 2;
            } else if ((0b1111_0000 & first) == 0b1110_0000) {
                if (remainingBytes < 3) {
                    throw new StreamCorruptedException("Incomplete three byte char");
                }

                byte second = dataIn.readByte();
                if ((0b1100_0000 & second) != 0b1000_0000) {
                    throw new StreamCorruptedException("Malformed string");
                }
                byte third = dataIn.readByte();
                if ((0b1100_0000 & third) != 0b1000_0000) {
                    throw new StreamCorruptedException("Malformed string");
                }

                c = (char) (
                    ((0b0000_1111 & first) << 12)
                    | ((0b0011_1111 & second) << 6)
                    | (0b0011_1111 & third)
                );
                if (c < 0x0800) {
                    throw new StreamCorruptedException("Char out of valid range");
                }
                remainingBytes -= 3;
            } else {
                throw new StreamCorruptedException("Malformed encoded char");
            }
            stringBuilder.append(c);
        }
        StringObject stringObject = new StringObject(stringBuilder.toString());
        handleManager.createAssignedHandle(stringObject);
        return stringObject;
    }

    private EnumConstantObject readNewEnum() throws IOException {
        ReadDescriptor<NonProxyDescriptorData> readClassDesc = readNonNullNonProxyClassDesc();
        var handle = handleManager.newUnassignedHandle(EnumConstantObject.class);
        ReadString constantString = readNonNullStringOrRef();

        NonProxyDescriptorData classDesc = readClassDesc.descriptor();
        validateWellformedEnumClassDesc(classDesc);

        boolean isMissingEnumSuperType;
        Optional<NonProxyDescriptorData> optSuperClassDesc = classDesc.superClassDesc();
        if (optSuperClassDesc.isPresent()) {
            isMissingEnumSuperType = false;
            NonProxyDescriptorData superClassDesc = optSuperClassDesc.get();
            String typeName = superClassDesc.typeName();
            if (!typeName.equals("java.lang.Enum")) {
                throw new UnsupportedStreamFeatureException("Enum super class " + typeName);
            }
            validateWellformedEnumClassDesc(superClassDesc);

            Optional<NonProxyDescriptorData> optSuperSuperClassDesc = superClassDesc.superClassDesc();
            if (optSuperSuperClassDesc.isPresent()) {
                throw new UnsupportedStreamFeatureException("Enum super super class descriptor " + optSuperSuperClassDesc);
            }
        } else {
            isMissingEnumSuperType = true;
        }

        EnumConstantObject enumConstantObject = new EnumConstantObject(
            classDesc.typeName(),
            constantString.value(),
            isMissingEnumSuperType,
            constantString.isHandle() || readClassDesc.isHandle() || classDesc.usesAnyHandle()
        );
        handle.setObject(enumConstantObject);
        return enumConstantObject;
    }

    private static void validateWellformedEnumClassDesc(NonProxyDescriptorData descriptor) throws IOException {
        long serialVersionUid = descriptor.serialVersionUid();
        if (serialVersionUid != 0) {
            throw new StreamCorruptedException("Invalid serial version UID: " + serialVersionUid);
        }

        byte flags = descriptor.flags();
        if (flags != (SC_SERIALIZABLE | SC_ENUM)) {
            throw new UnsupportedStreamFeatureException("Enum descriptor flags: " + flags);
        }

        List<PrimitiveField> primitiveFields = descriptor.primitiveFields();
        if (!primitiveFields.isEmpty()) {
            throw new UnsupportedStreamFeatureException("Enum primitive fields " + primitiveFields);
        }

        List<ObjectField> objectFields = descriptor.objectFields();
        if (!objectFields.isEmpty()) {
            throw new UnsupportedStreamFeatureException("Enum object fields" + objectFields);
        }
    }

    private DescriptorStreamObject readNewClassDesc(byte type) throws IOException {
        return switch(type) {
            case TC_CLASSDESC -> readNewNonProxyClassDesc();
            case TC_PROXYCLASSDESC -> readNewProxyClassDesc();
            default -> throw new StreamCorruptedException("Unknown class desc type: " + hexFormat.toHexDigits(type));
        };
    }

    private NonProxyDescriptorData readNewNonProxyClassDesc() throws IOException {
        String typeName = TypeNameHelper.createClassTypeName(dataIn.readUTF(), false);
        long serialVersionUid = dataIn.readLong();
        var handle = handleManager.newUnassignedHandle(NonProxyDescriptorData.class);
        byte flags = dataIn.readByte();
        short fieldsCount = dataIn.readShort();
        if (fieldsCount < 0) {
            throw new StreamCorruptedException("Invalid fields count: " + fieldsCount);
        }

        List<PrimitiveField> primitiveFields = new ArrayList<>();
        List<ObjectField> objectFields = new ArrayList<>();

        for (int i = 0; i < fieldsCount; i++) {
            char typeCode = (char) dataIn.readByte();
            String fieldName = dataIn.readUTF();

            PrimitiveFieldType primitiveFieldType = switch (typeCode) {
                case 'B' -> PrimitiveFieldType.BYTE;
                case 'C' -> PrimitiveFieldType.CHAR;
                case 'D' -> PrimitiveFieldType.DOUBLE;
                case 'F' -> PrimitiveFieldType.FLOAT;
                case 'I' -> PrimitiveFieldType.INT;
                case 'J' -> PrimitiveFieldType.LONG;
                case 'S' -> PrimitiveFieldType.SHORT;
                case 'Z' -> PrimitiveFieldType.BOOLEAN;
                case '[', 'L' -> {
                    ReadString fieldType = readNonNullStringOrRef();
                    String fieldJvmTypeName = fieldType.value();
                    if (fieldJvmTypeName.charAt(0) != typeCode) {
                        throw new StreamCorruptedException("Mismatching type code and field type name: " + fieldJvmTypeName);
                    }

                    String fieldTypeName = TypeNameHelper.createClassTypeName(fieldJvmTypeName, true);
                    objectFields.add(new ObjectField(fieldName, fieldTypeName, fieldType.isHandle()));
                    // Not a primitive field
                    yield null;
                }
                default -> throw new StreamCorruptedException("Unknown field type code: " + typeCode);
            };

            if (primitiveFieldType != null) {
                primitiveFields.add(new PrimitiveField(fieldName, primitiveFieldType));
            }
        }

        skipClassAnnotation();

        // Read non-proxy because proxy as super class is not supported
        Optional<ReadDescriptor<NonProxyDescriptorData>> optReadSuperDesc = readNonProxyClassDesc();
        Optional<NonProxyDescriptorData> superDesc;
        boolean usesSuperDescHandle;
        if (optReadSuperDesc.isPresent()) {
            ReadDescriptor<NonProxyDescriptorData> readSuperDesc = optReadSuperDesc.get();
            usesSuperDescHandle = readSuperDesc.isHandle();
            superDesc = Optional.of(readSuperDesc.descriptor());
        } else {
            superDesc = Optional.empty();
            usesSuperDescHandle = false;
        }

        NonProxyDescriptorData classDesc = new NonProxyDescriptorData(typeName, serialVersionUid, flags, primitiveFields, objectFields, superDesc, usesSuperDescHandle);

        // Note: Correct behavior would be to already assign handle before reading super class, however because
        //circular class descriptor hierarchies are not supported here, handle can be assigned here
        handle.setObject(classDesc);

        return classDesc;
    }

    private record ReadString(String value, boolean isHandle) {
    }

    private ReadString readNonNullStringOrRef() throws IOException {
        byte type = dataIn.readByte();
        return switch (type) {
            case TC_STRING, TC_LONGSTRING -> new ReadString(readNewString(type).value(), false);
            // In theory 'null' is allowed by grammar, but it is not supported as value
            case TC_NULL -> throw new StreamCorruptedException("Unsupported TC_NULL for string");
            case TC_REFERENCE -> {
                StreamObject referencedObject = readPrevAssignedObject();
                if (referencedObject instanceof StringObject stringObject) {
                    yield new ReadString(stringObject.value(), true);
                }
                throw new StreamCorruptedException("Reference to non-string object");
            }
            default -> throw new StreamCorruptedException("Unknown string type: " + hexFormat.toHexDigits(type));
        };
    }

    private ProxyDescriptorData readNewProxyClassDesc() throws IOException {
        var handle = handleManager.newUnassignedHandle(ProxyDescriptorData.class);

        int interfacesCount = dataIn.readInt();
        if (interfacesCount < 0) {
            throw new StreamCorruptedException("Invalid interfaces count: " + interfacesCount);
        }

        List<String> interfaceNames = new ArrayList<>();
        for (int i = 0; i < interfacesCount; i++) {
            interfaceNames.add(TypeNameHelper.createClassTypeName(dataIn.readUTF(), false));
        }
        skipClassAnnotation();

        // Don't validate super class here yet because proxy without super class is supported by builder for Class object
        Optional<ReadDescriptor<NonProxyDescriptorData>> optSuperClassDesc = readNonProxyClassDesc();
        ProxyDescriptorData proxyDescriptorData = new ProxyDescriptorData(
            interfaceNames,
            optSuperClassDesc.map(ReadDescriptor::descriptor),
            optSuperClassDesc.map(ReadDescriptor::isHandle).orElse(false)
        );

        // Note: Correct behavior would be to already assign handle before reading super class, however because
        //circular class descriptor hierarchies are not supported here, handle can be assigned here
        handle.setObject(proxyDescriptorData);
        return proxyDescriptorData;
    }

    private void skipClassAnnotation() throws IOException {
        byte b = dataIn.readByte();
        if (b != TC_ENDBLOCKDATA) {
            throw new UnsupportedStreamFeatureException("Class annotation data");
        }
    }

    private StreamObject readPrevAssignedObject() throws IOException {
        return readPrevObject().getReferencedObject().orElseThrow(() -> new UnsupportedStreamFeatureException("Circular reference"));
    }

    private HandleManager.Handle<?> readPrevObject() throws IOException {
        int handleIndex = dataIn.readInt() - baseWireHandle;
        return handleManager.getHandle(handleIndex).orElseThrow(() -> new StreamCorruptedException("Invalid handle index: " + handleIndex));
    }

    @Override
    public void close() throws IOException {
        dataIn.close();
    }
}
