package marcono1234.serialization.serialbuilder.codegen;

import marcono1234.serialization.serialbuilder.SimpleSerialBuilder;
import marcono1234.serialization.serialbuilder.builder.api.Handle;
import net.openhft.compiler.CompilerUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.support.AnnotationConsumer;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.PrintWriter;
import java.io.Serial;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

class SimpleSerialBuilderCodeGenTest {
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ArgumentsSource(SerializationDataProvider.class)
    @interface SerializationDataSource {
        enum Type {
            /** Serialization data for which code generation is successful */
            SUCCESSFUL(".expected-code.txt"),
            /** Serialization data for which code generation fails */
            FAILING(".expected-message.txt");

            final String expectedDataFileExtension;

            Type(String expectedDataFileExtension) {
                this.expectedDataFileExtension = expectedDataFileExtension;
            }
        }

        /** Resource path of the directory containing the serialization data */
        String value();

        /** Type of the provided serialization data */
        Type type() default Type.SUCCESSFUL;
    }

    private static String readNormalizedString(Path path) throws IOException {
        return Files.readString(path).replaceAll("\\R", "\n");
    }

    static class SerializationDataProvider implements ArgumentsProvider, AnnotationConsumer<SerializationDataSource> {
        private String directory;
        private String expectedDataFileExtension;

        @Override
        public void accept(SerializationDataSource serializationDataSource) {
            directory = serializationDataSource.value();
            expectedDataFileExtension = serializationDataSource.type().expectedDataFileExtension;
        }

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
            URL resourceUrl = getClass().getResource(directory);
            if (resourceUrl == null) {
                throw new ExtensionConfigurationException("Resource directory '" + directory + "' does not exist");
            }

            Map<String, Path> serialDataFiles = new LinkedHashMap<>();
            Map<String, Path> expectedDataFiles = new LinkedHashMap<>();

            try (Stream<Path> files = Files.list(Path.of(resourceUrl.toURI())).filter(Files::isRegularFile)) {
                String serialDataFileExtension = ".serial-data.txt";

                files.forEach(file -> {
                    String fileName = file.getFileName().toString();
                    if (fileName.endsWith(serialDataFileExtension)) {
                        String fileNameWithoutExtension = fileName.substring(0, fileName.length() - serialDataFileExtension.length());
                        serialDataFiles.put(fileNameWithoutExtension, file);
                    } else if (fileName.endsWith(expectedDataFileExtension)) {
                        String fileNameWithoutExtension = fileName.substring(0, fileName.length() - expectedDataFileExtension.length());
                        expectedDataFiles.put(fileNameWithoutExtension, file);
                    } else {
                        throw new ExtensionConfigurationException("File with unsupported extension: " + file);
                    }
                });
            }

            if (serialDataFiles.isEmpty()) {
                throw new ExtensionConfigurationException("No serial data files exist");
            }

            HexFormat hexFormat = HexFormat.of();
            Stream.Builder<Arguments> streamBuilder = Stream.builder();
            for (Map.Entry<String, Path> serialDataFileEntry : serialDataFiles.entrySet()) {
                String fileName = serialDataFileEntry.getKey();
                Path expectedDataFile = expectedDataFiles.remove(fileName);

                if (expectedDataFile == null) {
                    throw new ExtensionConfigurationException("Missing expected data file for: " + fileName);
                }

                streamBuilder.add(Arguments.of(
                    fileName,
                    hexFormat.parseHex(Files.readString(serialDataFileEntry.getValue())),
                    readNormalizedString(expectedDataFile)
                ));
            }

            if (!expectedDataFiles.isEmpty()) {
                throw new ExtensionConfigurationException("Missing serial data files for: " + expectedDataFiles.keySet());
            }
            return streamBuilder.build();
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @SerializationDataSource("/simple-api-codegen")
    // First parameter is used for display purposes
    void generateCode(@SuppressWarnings("unused") String fileName, byte[] serialData, String expectedCode) throws Exception {
        String actualCode = SimpleSerialBuilderCodeGen.generateCode(serialData);
        assertEquals(expectedCode, actualCode);

        compileAndExecuteGeneratedCode(actualCode, Optional.of(serialData));
    }

    /**
     * Tests code generation for serialization data which uses unsupported features. Only covers the cases
     * where code generation should be successful, but should emit comments in the generated code.
     */
    @ParameterizedTest(name = "[{index}] {0}")
    @SerializationDataSource("/simple-api-codegen-unsupported-features")
    // First parameter is used for display purposes
    void generateCode_UnsupportedFeatures(@SuppressWarnings("unused") String fileName, byte[] serialData, String expectedCode) throws Exception {
        String actualCode = SimpleSerialBuilderCodeGen.generateCode(serialData);
        assertEquals(expectedCode, actualCode);

        compileAndExecuteGeneratedCode(
            actualCode,
            // Don't compare serial data because it will differ due to usage of unsupported features
            Optional.empty()
        );
    }

    /**
     * @param code
     *      Serial builder Java code to compile and execute
     * @param expectedSerialData
     *      If present, represents the expected serial data which should be compared with the
     *      result of the executed code
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static void compileAndExecuteGeneratedCode(String code, Optional<byte[]> expectedSerialData) throws Exception {
        String packageName = SimpleSerialBuilderCodeGenTest.class.getPackageName();
        String className = "GeneratedClass";
        String qualifiedClassName = packageName + "." + className;
        String javaCode = (
            "package " + packageName + ";\n" +
            "\n" +
            "import " + SimpleSerialBuilder.class.getName() + ";\n" +
            "import " + Handle.class.getName() + ";\n" +
            "\n" +
            "public class " + className +" {\n" +
            "    public static byte[] createSerialData() {\n" +
            code + "\n" +
            "        return serialData;\n" +
            "    }\n" +
            "}"
        );

        // Load class with separate class loader
        try (URLClassLoader classLoader = new URLClassLoader(new URL[0], SimpleSerialBuilderCodeGenTest.class.getClassLoader())) {
            StringWriter messageWriter = new StringWriter();
            PrintWriter messagePrintWriter = new PrintWriter(messageWriter);
            Class<?> generatedClass;
            try {
                // Only seems to report compiler errors at the moment, see https://github.com/OpenHFT/Java-Runtime-Compiler/issues/93
                generatedClass = CompilerUtils.CACHED_COMPILER.loadFromJava(classLoader, qualifiedClassName, javaCode, messagePrintWriter);
            } catch (Exception e) {
                messagePrintWriter.flush();
                String message = messageWriter.toString();
                fail("Compilation failed: " + message, e);
                throw new AssertionError("unreachable");
            }

            messagePrintWriter.flush();
            String message = messageWriter.toString();
            if (!message.isEmpty()) {
                fail("Compilation warning: " + message);
            }

            byte[] generatedSerialData = (byte[]) generatedClass.getMethod("createSerialData").invoke(null);
            expectedSerialData.ifPresent(expectedData -> assertArrayEquals(expectedData, generatedSerialData));
        }
    }

    /**
     * Tests code generation for serialization data which uses unsupported features, and for which a
     * {@link CodeGenException} is thrown.
     */
    @ParameterizedTest(name = "[{index}] {0}")
    @SerializationDataSource(value = "/simple-api-codegen-unsupported-features-failing", type = SerializationDataSource.Type.FAILING)
    // First parameter is used for display purposes
    void generateCode_UnsupportedFeatures_Failing(@SuppressWarnings("unused") String fileName, byte[] serialData, String expectedExceptionMessage) {
        var e = assertThrows(CodeGenException.class, () -> SimpleSerialBuilderCodeGen.generateCode(serialData));
        assertEquals(expectedExceptionMessage, e.getMessage());
    }

    private record SerializableRecord(int i, String s) implements Serializable { }

    @Test
    void generateCode_SerializableObject() throws CodeGenException {
        assertEquals(
            """
            byte[] serialData = SimpleSerialBuilder.startSerializableObject()
                .beginClassData("marcono1234.serialization.serialbuilder.codegen.SimpleSerialBuilderCodeGenTest$SerializableRecord", 0L)
                    .primitiveIntField("i", 1)
                    .beginObjectField("s", "java.lang.String")
                        .string("a")
                    .endField()
                .endClassData()
            .endObject();
            """,
            SimpleSerialBuilderCodeGen.generateCode(new SerializableRecord(1, "a"))
        );
        assertEquals(
            """
            byte[] serialData = SimpleSerialBuilder.writeSerializationDataWith(writer -> {
                writer.string("test");
            });
            """,
            SimpleSerialBuilderCodeGen.generateCode("test")
        );
        assertEquals(
            """
            byte[] serialData = SimpleSerialBuilder.writeSerializationDataWith(writer -> {
                writer.nullObject();
            });
            """,
            SimpleSerialBuilderCodeGen.generateCode((Serializable) null)
        );
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ArgumentsSource(ClassDataProvider.class)
    @interface ClassDataSource {
        /** Resource path of the directory containing the expected code */
        String expectedCodeDir();
        /** Classes to generate code for */
        Class<?>[] classes();
        /** Name of method providing classes which cannot be referenced statically */
        String classesProviderMethod() default "";
    }

    static class ClassDataProvider implements ArgumentsProvider, AnnotationConsumer<ClassDataSource> {
        private String expectedCodeDir;
        private Class<?>[] classes;
        private String classesProviderMethodName;

        @Override
        public void accept(ClassDataSource classDataSource) {
            expectedCodeDir = classDataSource.expectedCodeDir();
            classes = classDataSource.classes();
            classesProviderMethodName = classDataSource.classesProviderMethod();
        }

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
            URL resourceUrl = getClass().getResource(expectedCodeDir);
            if (resourceUrl == null) {
                throw new ExtensionConfigurationException("Resource directory '" + expectedCodeDir + "' does not exist");
            }

            Map<Class<?>, String> classesToName = new LinkedHashMap<>();
            for (Class<?> c : classes) {
                classesToName.put(c, c.getSimpleName());
            }

            if (!classesProviderMethodName.isEmpty()) {
                Method classesProviderMethod;
                try {
                    classesProviderMethod = SimpleSerialBuilderCodeGenTest.class.getDeclaredMethod(classesProviderMethodName);
                } catch (NoSuchMethodException e) {
                    throw new ExtensionConfigurationException("Provider method '" + classesProviderMethodName + "' does not exist", e);
                }
                if (!Modifier.isStatic(classesProviderMethod.getModifiers())) {
                    throw new ExtensionConfigurationException("Provider method '" + classesProviderMethodName + "' is not static");
                }
                if (classesProviderMethod.getReturnType() != Stream.class) {
                    throw new ExtensionConfigurationException("Provider method '" + classesProviderMethodName + "' has wrong return type");
                }

                // Use Entry<Class, String> to allow specifying custom name, since some class names (e.g. of proxy)
                // might be implementation dependent
                @SuppressWarnings("unchecked")
                Stream<Map.Entry<Class<?>, String>> additionalClasses = (Stream<Map.Entry<Class<?>, String>>) classesProviderMethod.invoke(null);
                additionalClasses.forEach(entry -> {
                    Class<?> c = entry.getKey();
                    var existing = classesToName.put(c, entry.getValue());
                    if (existing != null) {
                        throw new ExtensionConfigurationException("Duplicate class " + c);
                    }
                });
            }

            Map<String, Path> expectedFilesMap;
            try (Stream<Path> files = Files.list(Path.of(resourceUrl.toURI())).filter(Files::isRegularFile)) {
                // Wrap in HashMap to make sure map is mutable
                expectedFilesMap = new HashMap<>(files.collect(Collectors.toMap(p -> p.getFileName().toString(), p -> p)));
            }

            Stream.Builder<Arguments> streamBuilder = Stream.builder();
            List<String> classNamesWithMissingCode = new ArrayList<>();
            for (Map.Entry<Class<?>, String> entry : classesToName.entrySet()) {
                Class<?> c = entry.getKey();
                String className = entry.getValue();
                Path expectedCodePath = expectedFilesMap.remove(className + ".expected-code.txt");
                if (expectedCodePath == null) {
                    classNamesWithMissingCode.add(className);
                    continue;
                }

                streamBuilder.add(Arguments.of(
                    className,
                    c,
                    readNormalizedString(expectedCodePath)
                ));
            }
            if (!classNamesWithMissingCode.isEmpty()) {
                throw new ExtensionConfigurationException("Missing expected code files for " + classNamesWithMissingCode);
            }
            if (!expectedFilesMap.isEmpty()) {
                throw new ExtensionConfigurationException("Unused files: " + expectedFilesMap.keySet());
            }

            return streamBuilder.build();
        }
    }

    private enum EnumClass {
        A {}
    }

    private record RecordClass(int i) implements Serializable {
    }

    @SuppressWarnings("unused")
    private static class SerializableClass implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private static final class FinalNonSerializableClass {
            private byte b;
        }

        private static class NonFinalSerializableClass implements Serializable {
            @Serial
            private static final long serialVersionUID = 1L;

            private boolean b;
            private FinalSerializableClass nested1;
            private RecordClass nested2;
            // Also test recursion through field types
            private NonFinalSerializableClass recursive;
        }

        private static final class FinalSerializableClass implements Serializable {
            @Serial
            private static final long serialVersionUID = 1L;

            private double d;
            private RecordClass nested;
            // Also test recursion through field types
            private FinalSerializableClass recursive;
        }

        private int i;
        private String s;
        private Enum<?> enumClass;
        private EnumClass e;
        private Class<?> c;
        private RecordClass r;
        // For custom final serializable class should include its class structure in generated code
        private FinalSerializableClass finalClass;
        private NonFinalSerializableClass nonFinal;
        private ExternalizableClass nonFinalExternalizable;
        private AbstractSerializableClass abstractClass;
        // Non-serializable class should not cause code generation to fail
        private FinalNonSerializableClass n;
        private int[] ints;
        private Map.Entry<?, ?>[] objects;
        private FinalSerializableClass[][] arrayOfFinalClass;
        private NonFinalSerializableClass[] nonFinalArray;
    }

    @SuppressWarnings("unused")
    private static class SerializableClassWithWriteObject implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private int i;
        private String s;

        @Serial
        private void writeObject(ObjectOutputStream objOut) {
        }
    }

    @SuppressWarnings("unused")
    private static class SerializableClassWithWriteReplace implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private int i;
        private String s;

        @Serial
        Object writeReplace() {
            return "test";
        }
    }

    @SuppressWarnings("unused")
    private static class SerializableSubclass extends SerializableClassWithWriteReplace {
        @Serial
        private static final long serialVersionUID = 2L;

        private float f;
    }

    @SuppressWarnings("unused")
    private static class SubclassOverridingWriteReplace extends SerializableClassWithWriteReplace {
        @Serial
        private static final long serialVersionUID = 2L;

        private byte b;

        @Serial
        @Override
        Object writeReplace() {
            return "overridden";
        }
    }

    @SuppressWarnings("unused")
    private static abstract class AbstractSerializableClass implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private short s;
    }

    private static class ExternalizableClass implements Externalizable {
        @Serial
        private static final long serialVersionUID = 1L;

        public ExternalizableClass() {
        }

        @Override
        public void writeExternal(ObjectOutput out) {
        }

        @Override
        public void readExternal(ObjectInput in) {
        }
    }

    /**
     * Replaces the placeholder comments in code generated by {@link SimpleSerialBuilderCodeGen#generateCodeForClass(Class, boolean, boolean)}
     * with mock values.
     */
    private static String replaceClassCodePlaceholders(String code) {
        return code
            // Primitive value (second capturing group)
            .replaceAll("(BooleanField.*)(/\\* value \\*/)","$1 true")
            .replaceAll("(ByteField.*)(/\\* value \\*/)","$1 (byte) 1")
            .replaceAll("(CharField.*)(/\\* value \\*/)","$1 'a'")
            .replaceAll("(ShortField.*)(/\\* value \\*/)","$1 (short) 1")
            .replaceAll("(IntField.*)(/\\* value \\*/)","$1 1")
            .replaceAll("(LongField.*)(/\\* value \\*/)","$1 1")
            .replaceAll("(FloatField.*)(/\\* value \\*/)","$1 1")
            .replaceAll("(DoubleField.*)(/\\* value \\*/)","$1 1")
            // Primitive array
            .replace("/* boolean array */", "new boolean[0]")
            .replace("/* byte array */", "new byte[0]")
            .replace("/* char array */", "new char[0]")
            .replace("/* short array */", "new short[0]")
            .replace("/* int array */", "new int[0]")
            .replace("/* long array */", "new long[0]")
            .replace("/* float array */", "new float[0]")
            .replace("/* double array */", "new double[0]")
            // Enum constant
            .replace("/* enum class */", "\"MyEnum\"")
            .replace("/* constant name */", "\"A\"")
            // String value
            .replaceAll("(?<=string\\()/\\* value \\*/", "\"a\"")
            // Class literal
            .replaceAll("(?<=class_\\()(/\\* value \\*/)", "Object.class")
            // Proxy invocation handler
            .replaceAll("/\\*\\s+\\*\\s+\\.\\.\\. invocation handler\\s+\\*/", ".externalizableInvocationHandler(\"MyHandler\", 1, writer -> {})")
            // Object field value
            .replaceAll("/\\*\\s+\\*\\s+\\.\\.\\. field data\\s+\\*/", ".nullObject()")
            ;
    }

    private static Stream<Map.Entry<Class<?>, String>> dynamicTopLevelClasses() {
        class InvocationHandlerImpl implements InvocationHandler {
            @SuppressWarnings("SuspiciousInvocationHandlerImplementation")
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                return null;
            }
        }
        Class<?> proxyClass = Proxy.newProxyInstance(null, new Class[] {Runnable.class, Callable.class}, new InvocationHandlerImpl()).getClass();

        return Stream.of(
            Map.entry(proxyClass, "ProxyClass")
        );
    }

    // Invoked through reflection
    @SuppressWarnings("unused")
    private static Stream<Map.Entry<Class<?>, String>> dynamicClasses() {
        return Stream.concat(dynamicTopLevelClasses(), Stream.of(
            // For anonymous enum subclass generated code should use declaring enum class
            Map.entry(EnumClass.A.getClass(), "EnumSubclass")
        ));
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @ClassDataSource(expectedCodeDir = "/simple-api-codegen-class", classes = {
        SerializableClass.class,
        SerializableClassWithWriteObject.class,
        SerializableClassWithWriteReplace.class,
        SerializableSubclass.class,
        SubclassOverridingWriteReplace.class,
        AbstractSerializableClass.class,
        ExternalizableClass.class,
        RecordClass.class,
        String.class,
        Class.class,
        Enum.class,
        EnumClass.class,
        int[].class,
        Map.Entry[].class,
    }, classesProviderMethod = "dynamicClasses")
    // First parameter is used for display purposes
    void generateCodeForClass(@SuppressWarnings("unused") String typeName, Class<?> c, String expectedCode) throws Exception {
        String actualCode = SimpleSerialBuilderCodeGen.generateCodeForClass(c, false, false);
        assertEquals(expectedCode, actualCode);

        String fullBuilderCode =
            """
            byte[] serialData = SimpleSerialBuilder.startSerializableObject()
                .beginClassData("MyClass", 1L)
                    .beginObjectField("f", "java.lang.Object")
            """
            + replaceClassCodePlaceholders(actualCode) +
            """
                    .endField()
                .endClassData()
            .endObject();
            """;
        compileAndExecuteGeneratedCode(fullBuilderCode, Optional.empty());
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @ClassDataSource(expectedCodeDir = "/simple-api-codegen-class-top-level", classes = {
        SerializableClass.class,
        SerializableClassWithWriteObject.class,
        SerializableClassWithWriteReplace.class,
        SerializableSubclass.class,
        SubclassOverridingWriteReplace.class,
        AbstractSerializableClass.class,
        ExternalizableClass.class,
        RecordClass.class,
    }, classesProviderMethod = "dynamicTopLevelClasses")
    // First parameter is used for display purposes
    void generateCodeForClass_TopLevel(@SuppressWarnings("unused") String typeName, Class<?> c, String expectedCode) throws Exception {
        String actualCode = SimpleSerialBuilderCodeGen.generateCodeForClass(c, true, false);
        assertEquals(expectedCode, actualCode);

        compileAndExecuteGeneratedCode(
            replaceClassCodePlaceholders(actualCode),
            Optional.empty()
        );
    }

    /**
     * Tests {@link SimpleSerialBuilderCodeGen#generateCodeForClass(Class, boolean, boolean)} with
     * {@code generateForNestedNonFinal = true}.
     */
    @ParameterizedTest(name = "[{index}] {0}")
    @ClassDataSource(expectedCodeDir = "/simple-api-codegen-class-top-level-non-final", classes = {
        SerializableClass.class,
    })
    // First parameter is used for display purposes
    void generateCodeForClass_TopLevel_NonFinal(@SuppressWarnings("unused") String typeName, Class<?> c, String expectedCode) throws Exception {
        String actualCode = SimpleSerialBuilderCodeGen.generateCodeForClass(c, true, true);
        assertEquals(expectedCode, actualCode);

        compileAndExecuteGeneratedCode(
            replaceClassCodePlaceholders(actualCode),
            Optional.empty()
        );
    }

    @ParameterizedTest
    @ValueSource(classes = {
        String.class,
        Class.class,
        ObjectStreamClass.class,
        Enum.class,
        EnumClass.class,
        int[].class,
        Map.Entry[].class,
    })
    void generateCodeForClass_TopLevel_Unsupported(Class<?> c) {
        var e = assertThrows(CodeGenException.class, () -> SimpleSerialBuilderCodeGen.generateCodeForClass(c, true, false));
        assertEquals("Unsupported top level type " + c.getTypeName(), e.getMessage());
    }

    @Test
    void generateCodeForClass_Unsupported_ObjectStreamClass() {
        var e = assertThrows(CodeGenException.class, () -> SimpleSerialBuilderCodeGen.generateCodeForClass(ObjectStreamClass.class, false, false));
        assertEquals("Writing ObjectStreamClass is unsupported", e.getMessage());

        e = assertThrows(CodeGenException.class, () -> SimpleSerialBuilderCodeGen.generateCodeForClass(ObjectStreamClass.class, true, false));
        assertEquals("Unsupported top level type java.io.ObjectStreamClass", e.getMessage());
    }

    @Test
    void generateCodeForClass_NonSerializable() {
        class NonSerializableClass {
        }

        String expectedMessage = "Class " + NonSerializableClass.class.getTypeName() + " does not implement Serializable";

        var e = assertThrows(CodeGenException.class, () -> SimpleSerialBuilderCodeGen.generateCodeForClass(NonSerializableClass.class, false, false));
        assertEquals(expectedMessage, e.getMessage());

        e = assertThrows(CodeGenException.class, () -> SimpleSerialBuilderCodeGen.generateCodeForClass(NonSerializableClass.class, true, false));
        assertEquals(expectedMessage, e.getMessage());
    }

    private interface InterfaceExtendingSerializable extends Serializable {
    }
    private interface InterfaceExtendingExternalizable extends Externalizable {
    }

    @ParameterizedTest
    @ValueSource(classes = {
        Serializable.class,
        InterfaceExtendingSerializable.class,
        Externalizable.class,
        InterfaceExtendingExternalizable.class,
    })
    void generateCodeForClass_Interface(Class<?> c) {
        String expectedMessage = "Code generation for interface (" + c.getTypeName() + ") is not supported";

        var e = assertThrows(CodeGenException.class, () -> SimpleSerialBuilderCodeGen.generateCodeForClass(c, false, false));
        assertEquals(expectedMessage, e.getMessage());

        e = assertThrows(CodeGenException.class, () -> SimpleSerialBuilderCodeGen.generateCodeForClass(c, true, false));
        assertEquals(expectedMessage, e.getMessage());
    }

    private abstract static class AbstractExternalizable implements Externalizable {
    }

    @Test
    void generateCodeForClass_AbstractExternalizable() {
        String expectedMessage = "Code generation for abstract Externalizable (" + AbstractExternalizable.class.getTypeName() + ") is not supported";

        var e = assertThrows(CodeGenException.class, () -> SimpleSerialBuilderCodeGen.generateCodeForClass(AbstractExternalizable.class, false, false));
        assertEquals(expectedMessage, e.getMessage());

        e = assertThrows(CodeGenException.class, () -> SimpleSerialBuilderCodeGen.generateCodeForClass(AbstractExternalizable.class, true, false));
        assertEquals(expectedMessage, e.getMessage());
    }
}
