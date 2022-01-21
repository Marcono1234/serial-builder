package marcono1234.serialization.serialbuilder.builder.implementation;

import marcono1234.serialization.serialbuilder.builder.api.Enclosing;
import marcono1234.serialization.serialbuilder.builder.api.Handle;
import marcono1234.serialization.serialbuilder.builder.api.descriptor.DescriptorEnd;
import marcono1234.serialization.serialbuilder.builder.api.descriptor.DescriptorHierarchyStart;
import marcono1234.serialization.serialbuilder.builder.api.descriptor.DescriptorStart;
import marcono1234.serialization.serialbuilder.builder.api.descriptor.DescriptorsList;
import marcono1234.serialization.serialbuilder.builder.api.descriptor.nonproxy.NonProxyDescriptorFlags;
import marcono1234.serialization.serialbuilder.builder.api.descriptor.nonproxy.NonProxyDescriptorObjectFields;
import marcono1234.serialization.serialbuilder.builder.api.descriptor.nonproxy.NonProxyDescriptorObjectFieldsStart;
import marcono1234.serialization.serialbuilder.builder.api.descriptor.nonproxy.NonProxyDescriptorPrimitiveFields;
import marcono1234.serialization.serialbuilder.builder.api.descriptor.nonproxy.NonProxyDescriptorPrimitiveFieldsStart;
import marcono1234.serialization.serialbuilder.builder.api.descriptor.nonproxy.NonProxyDescriptorSerialVersionUid;
import marcono1234.serialization.serialbuilder.builder.api.descriptor.nonproxy.NonProxyDescriptorStart;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.io.ObjectStreamConstants.TC_CLASSDESC;
import static java.io.ObjectStreamConstants.TC_ENDBLOCKDATA;
import static java.io.ObjectStreamConstants.TC_NULL;
import static java.io.ObjectStreamConstants.TC_PROXYCLASSDESC;
import static java.io.ObjectStreamConstants.TC_REFERENCE;
import static java.io.ObjectStreamConstants.baseWireHandle;

@SuppressWarnings("rawtypes")
public class DescriptorHierarchyBuilderImpl implements DescriptorHierarchyStart, DescriptorsList, DescriptorStart, NonProxyDescriptorStart, NonProxyDescriptorSerialVersionUid, NonProxyDescriptorFlags, NonProxyDescriptorPrimitiveFieldsStart, NonProxyDescriptorPrimitiveFields, NonProxyDescriptorObjectFieldsStart, NonProxyDescriptorObjectFields, Enclosing {
    private final Object parent;
    private final AtomicInteger nextHandleIndex;
    /** Handle which is assigned after descriptor hierarchy has been written */
    private final Handle postDescriptorHierarchyHandle;
    private final Consumer<Consumer<UncheckedBlockDataOutputStream>> actionsConsumer;

    private static Handle verifyUnassigned(Handle handle) {
        if (handle.isAssigned()) {
            throw new IllegalArgumentException("Handle is already assigned an object index: " + handle);
        }
        return handle;
    }

    public DescriptorHierarchyBuilderImpl(Object parent, AtomicInteger nextHandleIndex, Handle postDescriptorHierarchyHandle, Consumer<Consumer<UncheckedBlockDataOutputStream>> actionsConsumer) {
        this.parent = parent;
        this.nextHandleIndex = nextHandleIndex;
        this.postDescriptorHierarchyHandle = verifyUnassigned(postDescriptorHierarchyHandle);
        this.actionsConsumer = actionsConsumer;
    }

    /**
     * Converts a type name in the format of {@link Class#getTypeName()} to the format
     * used by {@link Class#getName()}.
     */
    private static String typeNameToClassGetName(String typeName) {
        if (!typeName.endsWith("[]")) {
            return typeName;
        } else {
            StringBuilder classGetNameBuilder = new StringBuilder();
            String elementTypeName = typeName;

            while (elementTypeName.endsWith("[]")) {
                classGetNameBuilder.append('[');
                elementTypeName = elementTypeName.substring(0, elementTypeName.length() - 2);
            }

            String elementJvmTypeName = switch (elementTypeName) {
                case "byte" -> "B";
                case "char" -> "C";
                case "double" -> "D";
                case "float" -> "F";
                case "int" -> "I";
                case "long" -> "J";
                case "short" -> "S";
                case "boolean" -> "Z";
                // Note: Class.getName() does not replace '.' with '/'
                default -> 'L' + elementTypeName + ';';
            };

            return classGetNameBuilder.append(elementJvmTypeName).toString();
        }
    }

    @Override
    public DescriptorsList beginDescriptorHierarchy() {
        return this;
    }

    private Object finishAndGetParent() {
        HandleAccess.INSTANCE.assignIndex(postDescriptorHierarchyHandle, nextHandleIndex.getAndIncrement());
        return parent;
    }

    @Override
    public Object descriptorHierarchy(Function writer) {
        @SuppressWarnings("unchecked")
        var returnedBuilder = writer.apply(beginDescriptorHierarchy());
        if (returnedBuilder != parent) {
            throw new IllegalStateException("Incorrect builder usage");
        }

        // No need to call finishAndGetParent(); `writer` already did that by ending hierarchy
        return parent;
    }

    @Override
    public DescriptorStart proxyDescriptor(Handle unassignedHandle, String... interfaceNames) {
        int handleIndex = nextHandleIndex.getAndIncrement();
        HandleAccess.INSTANCE.assignIndex(unassignedHandle, handleIndex);

        // Clone array to prevent accidental modification in the meantime
        String[] namesCopy = interfaceNames.clone();
        actionsConsumer.accept(out -> {
            out.writeByte(TC_PROXYCLASSDESC);
            out.writeInt(namesCopy.length);
            for (String name : namesCopy) {
                out.writeUTF(typeNameToClassGetName(name));
            }

            out.setBlockDataMode(true);
            out.setBlockDataMode(false);
            out.writeByte(TC_ENDBLOCKDATA);
        });
        return this;
    }

    private List<Consumer<UncheckedBlockDataOutputStream>> fieldActions;

    @Override
    public NonProxyDescriptorStart<DescriptorStart> beginDescriptor(Handle unassignedHandle) {
        int handleIndex = nextHandleIndex.getAndIncrement();
        HandleAccess.INSTANCE.assignIndex(unassignedHandle, handleIndex);

        actionsConsumer.accept(out -> out.writeByte(TC_CLASSDESC));
        fieldActions = new ArrayList<>();

        @SuppressWarnings("unchecked")
        NonProxyDescriptorStart<DescriptorStart> result = this;
        return result;
    }

    private String descriptorName;

    @Override
    public NonProxyDescriptorSerialVersionUid typeName(String name) {
        descriptorName = typeNameToClassGetName(name);
        return this;
    }

    private long uid;

    @Override
    public NonProxyDescriptorFlags uid(long uid) {
        this.uid = uid;
        return this;
    }

    private byte flags;

    @Override
    public NonProxyDescriptorPrimitiveFieldsStart flags(int flags) {
        this.flags = (byte) flags;
        return this;
    }

    @Override
    public NonProxyDescriptorPrimitiveFields beginPrimitiveFieldDescriptors() {
        return this;
    }

    private static String getJvmTypeName(String typeName) {
        return switch (typeName) {
            case "byte" -> "B";
            case "char" -> "C";
            case "double" -> "D";
            case "float" -> "F";
            case "int" -> "I";
            case "long" -> "J";
            case "short" -> "S";
            case "boolean" -> "Z";
            default -> 'L' + typeName.replace('.', '/') + ';';
        };
    }

    @Override
    public NonProxyDescriptorPrimitiveFields primitiveField(String name, String typeName) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(typeName);

        String jvmTypeName = getJvmTypeName(typeName);
        if (jvmTypeName.length() != 1) {
            throw new IllegalArgumentException("Invalid primitive type name: " + typeName);
        }

        fieldActions.add(out -> {
            out.writeByte(jvmTypeName.charAt(0));
            out.writeUTF(name);
        });

        return this;
    }

    @Override
    public NonProxyDescriptorObjectFieldsStart endPrimitiveFieldDescriptors() {
        return this;
    }

    @Override
    public NonProxyDescriptorObjectFields beginObjectFieldDescriptors() {
        return this;
    }

    @Override
    public NonProxyDescriptorObjectFields objectField(String name, String typeName) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(typeName);

        StringBuilder jvmTypeNameBuilder = new StringBuilder();
        String elementTypeName = typeName;

        while (elementTypeName.endsWith("[]")) {
            jvmTypeNameBuilder.append('[');
            elementTypeName = elementTypeName.substring(0, elementTypeName.length() - 2);
        }

        String elementJvmTypeName = getJvmTypeName(elementTypeName);
        // If non-array (`typeName.equals(elementTypeName)`) check if type is primitive type
        if (typeName.equals(elementTypeName) && elementJvmTypeName.length() == 1) {
            throw new IllegalArgumentException("Primitive type not allowed for object field type: " + typeName);
        }

        String jvmTypeName = jvmTypeNameBuilder.append(elementJvmTypeName).toString();

        // Handle for type name string
        // Cannot perform this in the field action Runnable below because then it might be executed too late
        nextHandleIndex.getAndIncrement();

        fieldActions.add(out -> {
            out.writeByte(jvmTypeName.charAt(0));
            out.writeUTF(name);

            out.writeSerialString(jvmTypeName);
        });

        return this;
    }

    @Override
    public DescriptorEnd endObjectFieldDescriptors() {
        return this;
    }

    @Override
    public Object endDescriptor() {
        int fieldsCount = fieldActions.size();
        // Save values in local variables to ignore subsequent modifications
        String descriptorName = this.descriptorName;
        long uid = this.uid;
        byte flags = this.flags;
        actionsConsumer.accept(out -> {
            out.writeUTF(descriptorName);
            out.writeLong(uid);
            out.writeByte(flags);
            out.writeShort(fieldsCount);
        });
        fieldActions.forEach(actionsConsumer);
        actionsConsumer.accept(out -> {
            out.setBlockDataMode(true);
            out.setBlockDataMode(false);
            out.writeByte(TC_ENDBLOCKDATA);
        });

        fieldActions = null;
        return this;
    }

    @Override
    public Object endDescriptorHierarchyWithHandle(Handle handle) {
        int handleIndex = HandleAccess.INSTANCE.getObjectIndex(handle);
        actionsConsumer.accept(out -> {
            out.writeByte(TC_REFERENCE);
            out.writeInt(baseWireHandle + handleIndex);
        });
        return finishAndGetParent();
    }

    @Override
    public Object endDescriptorHierarchy() {
        actionsConsumer.accept(out -> {
            // Write final null at end of hierarchy
            out.writeByte(TC_NULL);
        });
        return finishAndGetParent();
    }
}
