package marcono1234.serialization.serialbuilder.codegen.implementation;

import marcono1234.serialization.serialbuilder.codegen.CodeGenException;
import marcono1234.serialization.serialbuilder.codegen.implementation.writer.CodeWriter;
import marcono1234.serialization.serialbuilder.codegen.implementation.writer.LiteralsHelper;

import java.io.Externalizable;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Generates a rough code structure for a serializable class.
 */
public class ClassCodeGen {
    private ClassCodeGen() {
    }

    public static String generateCode(Class<?> c, boolean generateTopLevel, boolean generateForNestedNonFinal) throws CodeGenException {
        Objects.requireNonNull(c);

        if (c.isInterface()) {
            throw new CodeGenException("Code generation for interface (" + c.getTypeName() + ") is not supported");
        }

        CodeWriter codeWriter = new CodeWriter(true, true);
        if (generateTopLevel) {
            writeTopLevelCode(codeWriter, c, generateForNestedNonFinal);
        } else {
            writeNonTopLevelCode(codeWriter, c, createRecursionTracker(Set.of()), generateForNestedNonFinal);
        }
        return codeWriter.getCode();
    }

    private static void writeTopLevelCode(CodeWriter codeWriter, Class<?> c, boolean generateForNestedNonFinal) throws CodeGenException {
        String startLinePrefix = "byte[] serialData = SimpleSerialBuilder.";

        // Types with custom serialization format, but without dedicated top level builder method
        if (Enum.class.isAssignableFrom(c) || c.isArray() || c == ObjectStreamClass.class || c == Class.class || c == String.class) {
            throw new CodeGenException("Unsupported top level type " + c.getTypeName());
        } else if (Proxy.isProxyClass(c)) {
            writeProxyCode(codeWriter, c, startLinePrefix + "startProxyObject", true);
        } else if (Externalizable.class.isAssignableFrom(c)) {
            writeExternalizableCode(codeWriter, c, startLinePrefix + "externalizableObject", true);
        } else if (Serializable.class.isAssignableFrom(c)) {
            writeSerializableCode(codeWriter, c, createRecursionTracker(Set.of()), startLinePrefix + "startSerializableObject", true, generateForNestedNonFinal);
        } else {
            throw new CodeGenException("Class " + c.getTypeName() + " does not implement Serializable");
        }
    }

    private static void writeProxyCode(CodeWriter codeWriter, Class<?> proxyClass, String firstLinePrefix, boolean isTopLevel) {
        StringBuilder firstLineBuilder = new StringBuilder(firstLinePrefix);
        firstLineBuilder.append('(');

        Iterator<String> interfaceNamesIterator = Arrays.stream(proxyClass.getInterfaces()).map(ClassCodeGen::createTypeNameLiteral).iterator();
        while (interfaceNamesIterator.hasNext()) {
            firstLineBuilder.append(interfaceNamesIterator.next());

            if (interfaceNamesIterator.hasNext()) {
                firstLineBuilder.append(", ");
            }
        }
        firstLineBuilder.append(')');

        codeWriter.writeLine(firstLineBuilder.toString());
        codeWriter.increaseIndentation();
        writeBlockComment(codeWriter, "... invocation handler");
        codeWriter.decreaseIndentation();
        codeWriter.writeLine(".endProxyObject()" + (isTopLevel ? ";" : ""));
    }

    private static boolean isAbstract(Class<?> c) {
        return Modifier.isAbstract(c.getModifiers());
    }

    private static void writeExternalizableCode(CodeWriter codeWriter, Class<?> externalizableClass, String firstLinePrefix, boolean isTopLevel) throws CodeGenException {
        if (isAbstract(externalizableClass)) {
            throw new CodeGenException("Code generation for abstract Externalizable (" + externalizableClass.getTypeName() + ") is not supported");
        }

        String firstLine = firstLinePrefix + "(" + createTypeNameLiteral(externalizableClass) + ", " + createSerialVersionUidLiteral(externalizableClass) + ", writer -> {";
        codeWriter.writeLine(firstLine);
        codeWriter.increaseIndentation();
        writeBlockComment(codeWriter, "... object data");
        codeWriter.decreaseIndentation();
        codeWriter.writeLine("})" + (isTopLevel ? ";" : ""));
    }

    private static void writeSerializableCode(CodeWriter codeWriter, Class<?> serializableClass, Set<Class<?>> recursionTracker, String firstLinePrefix, boolean isTopLevel, boolean generateForNestedNonFinal) throws CodeGenException {
        codeWriter.writeLine(firstLinePrefix + "()");
        codeWriter.increaseIndentation();
        writeClassHierarchyData(codeWriter, serializableClass, recursionTracker, generateForNestedNonFinal);
        codeWriter.decreaseIndentation();
        codeWriter.writeLine(".endObject()" + (isTopLevel ? ";" : ""));
    }

    private static void writeNonTopLevelCode(CodeWriter codeWriter, Class<?> c, Set<Class<?>> recursionTracker, boolean generateForNestedNonFinal) throws CodeGenException {
        String valuePlaceholder = "/* value */";

        if (Enum.class.isAssignableFrom(c)) {
            if (c == Enum.class) {
                codeWriter.writeLine(".enumConstant(/* enum class */, /* constant name */)");
            } else {
                // For anonymous subclasses get their superclass
                Class<?> enumClass = c.isEnum() ? c : c.getSuperclass();
                codeWriter.writeLine(".enumConstant(" + createTypeNameLiteral(enumClass) + ", /* constant name */)");
            }
        } else if (c.isArray()) {
            Class<?> componentType = c.getComponentType();
            if (componentType.isPrimitive()) {
                codeWriter.writeLine(".array(/* " + componentType.getName() + " array */)");
            } else {
                codeWriter.writeLine(".beginObjectArray(" + createTypeNameLiteral(c) + ")");
                codeWriter.increaseIndentation();

                Set<Class<?>> recursionTrackerClone = createRecursionTracker(recursionTracker);

                if (hasDefiniteSerialFormat(componentType, generateForNestedNonFinal) && recursionTrackerClone.add(componentType)) {
                    writeNonTopLevelCode(codeWriter, componentType, recursionTrackerClone, generateForNestedNonFinal);
                    // Write regular comment for optional more elements
                    codeWriter.writeComment("... more array elements");
                } else {
                    writeBlockComment(codeWriter, "... array elements");
                }
                codeWriter.decreaseIndentation();
                codeWriter.writeLine(".endArray()");
            }
        } else if (c == ObjectStreamClass.class) {
            throw new CodeGenException("Writing ObjectStreamClass is unsupported");
        } else if (c == Class.class) {
            codeWriter.writeLine(".class_(" + valuePlaceholder + ")");
        } else if (c == String.class) {
            codeWriter.writeLine(".string(" + valuePlaceholder + ")");
        } else if (Proxy.isProxyClass(c)) {
            writeProxyCode(codeWriter, c, ".beginProxyObject", false);
        } else if (Externalizable.class.isAssignableFrom(c)) {
            writeExternalizableCode(codeWriter, c, ".externalizableObject", false);
        } else if (Serializable.class.isAssignableFrom(c)) {
            writeSerializableCode(codeWriter, c, recursionTracker, ".beginSerializableObject", false, generateForNestedNonFinal);
        } else {
            throw new CodeGenException("Class " + c.getTypeName() + " does not implement Serializable");
        }
    }

    private static Set<Class<?>> createRecursionTracker(Set<Class<?>> existing) {
        Set<Class<?>> set = Collections.newSetFromMap(new IdentityHashMap<>());
        set.addAll(existing);
        return set;
    }

    /**
     * Returns whether the class has a definite serialization format, e.g. due to special format for types such as
     * enums, or because the class is final (includes arrays, Class and String). This can be used to determine
     * whether to generate code recursively.
     */
    private static boolean hasDefiniteSerialFormat(Class<?> c, boolean allowNonFinal) {
        // For enum `final` check does not work because it might have anonymous subclasses
        return Serializable.class.isAssignableFrom(c) && (
            Enum.class.isAssignableFrom(c) || Modifier.isFinal(c.getModifiers()) || (allowNonFinal && !isAbstract(c))
        );
    }

    private static void writeClassHierarchyData(CodeWriter codeWriter, Class<?> topClass, Set<Class<?>> recursionTracker, boolean generateForNestedNonFinal) throws CodeGenException {
        // Check for declared or inherited writeReplace() method
        Class<?> classDeclaringWriteReplace = getClassDeclaringWriteReplaceMethod(topClass);

        List<Class<?>> classHierarchy = new ArrayList<>();
        Class<?> currentClass = topClass;
        do {
            classHierarchy.add(currentClass);
        } while ((currentClass = currentClass.getSuperclass()) != null && Serializable.class.isAssignableFrom(currentClass));

        // Iterate in reverse order
        for (int i = classHierarchy.size() - 1; i >= 0; i--) {
            currentClass = classHierarchy.get(i);

            // Only report the writeReplace() method which takes effect; don't consider overridden ones (if any)
            if (currentClass == classDeclaringWriteReplace) {
                // Write comment because due to writeReplace() class might normally not be serialized itself
                codeWriter.writeComment("Class has writeReplace() method");
            }

            codeWriter.writeLine(".beginClassData(" + createTypeNameLiteral(currentClass) + ", " + createSerialVersionUidLiteral(currentClass) + ")");
            codeWriter.increaseIndentation();

            ObjectStreamField[] fields = ObjectStreamClass.lookup(currentClass).getFields();
            for (ObjectStreamField field : fields) {
                String fieldNameLiteral = LiteralsHelper.createStringLiteral(field.getName());
                Class<?> fieldType = field.getType();

                if (fieldType.isPrimitive()) {
                    String typeName = fieldType.getName();
                    // Upper case first char of primitive type name
                    String methodName = "primitive" + Character.toUpperCase(typeName.charAt(0)) + typeName.substring(1) + "Field";

                    codeWriter.writeLine("." + methodName + "(" + fieldNameLiteral + ", /* value */)");
                } else {
                    codeWriter.writeLine(".beginObjectField(" + fieldNameLiteral + ", " + createTypeNameLiteral(fieldType) + ")");
                    codeWriter.increaseIndentation();

                    Set<Class<?>> currentRecursionTracker = createRecursionTracker(recursionTracker);
                    currentRecursionTracker.add(currentClass);

                    if (hasDefiniteSerialFormat(fieldType, generateForNestedNonFinal) && currentRecursionTracker.add(fieldType)) {
                        writeNonTopLevelCode(codeWriter, fieldType, currentRecursionTracker, generateForNestedNonFinal);
                    } else {
                        writeBlockComment(codeWriter, "... field data");
                    }

                    codeWriter.decreaseIndentation();
                    codeWriter.writeLine(".endField()");
                }
            }

            if (hasWriteObjectMethod(currentClass)) {
                codeWriter.writeLine(".writeObjectWith(writer -> {");
                codeWriter.increaseIndentation();
                writeBlockComment(codeWriter, "... object data");
                codeWriter.decreaseIndentation();
                codeWriter.writeLine("})");
            }

            codeWriter.decreaseIndentation();
            codeWriter.writeLine(".endClassData()");
        }

        if (isAbstract(topClass)) {
            // Deserialization of abstract classes is not possible, write a warning
            codeWriter.writeComment("Warning: Class is abstract; must add class data of non-abstract subclass");
        }
    }

    private static void writeBlockComment(CodeWriter codeWriter, String text) {
        codeWriter.writeLine("/*");
        codeWriter.writeLine(" * " + text);
        codeWriter.writeLine(" */");
    }

    private static String createTypeNameLiteral(Class<?> c) {
        return LiteralsHelper.createStringLiteral(c.getTypeName());
    }

    private static String createSerialVersionUidLiteral(Class<?> c) {
        return LiteralsHelper.createLongLiteral(ObjectStreamClass.lookup(c).getSerialVersionUID());
    }

    private static boolean hasWriteObjectMethod(Class<?> c) {
        Method method;
        try {
            method = c.getDeclaredMethod("writeObject", ObjectOutputStream.class);
        } catch (NoSuchMethodException e) {
            return false;
        }

        int modifiers = method.getModifiers();
        return Modifier.isPrivate(modifiers) && !Modifier.isStatic(modifiers) && method.getReturnType() == void.class;
    }

    private static Class<?> getClassDeclaringWriteReplaceMethod(Class<?> c) {
        Class<?> declaringClass = c;
        Method method = null;

        // Roughly matches implementation of java.io.ObjectStreamClass.getInheritableMethod
        do {
            try {
                method = declaringClass.getDeclaredMethod("writeReplace");
                break;
            } catch (NoSuchMethodException e) {
                declaringClass = declaringClass.getSuperclass();
            }
        } while (declaringClass != null);

        if (method == null) {
            return null;
        }

        int modifiers = method.getModifiers();
        // Note: Does not check if method is abstract (in contrast to ObjectStreamClass implementation) to support
        // code generation for abstract classes, where one of the subclasses has to override `writeReplace` anyway
        if (Modifier.isStatic(modifiers) || method.getReturnType() != Object.class) {
            return null;
        }

        if (Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers)) {
            return declaringClass;
        } else if (Modifier.isPrivate(modifiers)) {
            return declaringClass == c ? declaringClass : null;
        }
        // package-private
        else {
            // Don't perform ClassLoader checks done by ObjectStreamClass; when class is actually
            // serialized at runtime the class loaders might differ
            if (c.getPackageName().equals(declaringClass.getPackageName())) {
                return declaringClass;
            } else {
                return null;
            }
        }
    }
}
