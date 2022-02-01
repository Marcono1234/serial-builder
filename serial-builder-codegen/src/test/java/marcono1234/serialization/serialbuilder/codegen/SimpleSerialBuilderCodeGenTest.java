package marcono1234.serialization.serialbuilder.codegen;

import marcono1234.serialization.serialbuilder.SimpleSerialBuilder;
import marcono1234.serialization.serialbuilder.builder.api.Handle;
import net.openhft.compiler.CompilerUtils;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.AnnotationConsumer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
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

        /** Name of the directory containing the serialization data */
        String value();

        /** Type of the provided serialization data */
        Type type() default Type.SUCCESSFUL;
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
                    Files.readString(expectedDataFile).replaceAll("\\R", "\n")
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
    void codeGeneration(String fileName, byte[] serialData, String expectedCode) throws Exception {
        String actualCode = SimpleSerialBuilderCodeGen.generateCode(serialData);
        assertEquals(expectedCode, actualCode);

        compileAndExecuteGeneratedCode(actualCode, serialData, true);
    }

    /**
     * Tests code generation for serialization data which uses unsupported features. Only covers the cases
     * where code generation should be successful, but should emit comments in the generated code.
     */
    @ParameterizedTest(name = "[{index}] {0}")
    @SerializationDataSource("/simple-api-codegen-unsupported-features")
    // First parameter is used for display purposes
    void codeGeneration_UnsupportedFeatures(String fileName, byte[] serialData, String expectedCode) throws Exception {
        String actualCode = SimpleSerialBuilderCodeGen.generateCode(serialData);
        assertEquals(expectedCode, actualCode);

        compileAndExecuteGeneratedCode(
            actualCode,
            serialData,
            // Don't compare serial data because it will differ due to usage of unsupported features
            false
        );
    }

    private static void compileAndExecuteGeneratedCode(String code, byte[] expectedSerialData, boolean compareGeneratedSerialData) throws Exception {
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
            if (compareGeneratedSerialData) {
                assertArrayEquals(expectedSerialData, generatedSerialData);
            }
        }
    }

    /**
     * Tests code generation for serialization data which uses unsupported features, and for which a
     * {@link CodeGenException} is thrown.
     */
    @ParameterizedTest(name = "[{index}] {0}")
    @SerializationDataSource(value = "/simple-api-codegen-unsupported-features-failing", type = SerializationDataSource.Type.FAILING)
    // First parameter is used for display purposes
    void codeGeneration_UnsupportedFeatures_Failing(String fileName, byte[] serialData, String expectedExceptionMessage) {
        var e = assertThrows(CodeGenException.class, () -> SimpleSerialBuilderCodeGen.generateCode(serialData));
        assertEquals(expectedExceptionMessage, e.getMessage());
    }
}
