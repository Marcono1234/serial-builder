# serial-builder-codegen

This project allows generating Java source code which recreates serialization data using the serial-builder API (as
close as possible). Code can either be generated programmatically or from the command line, see usage examples below.

The generated code only represents the snippet invoking the serial-builder API methods, it cannot be compiled on its
own. The following imports are necessary to use the generated code:
```java
import marcono1234.serialization.serialbuilder.SimpleSerialBuilder;
import marcono1234.serialization.serialbuilder.builder.api.Handle;
```

If the provided serialization data cannot be recreated exactly using the serial-builder API but where the difference
should normally not have any effect, an inline comment is written in the generated code. If the provided serialization
data is malformed, or if it cannot be recreated at all, an exception is thrown. That should normally only happen when
the data is valid according to the serialization format grammar, but cannot actually be read by `ObjectInputStream`, or
when it is not 'interesting', such as a top level `String` object, and is therefore not supported by the serial-builder
API. If you are missing support for a use case, feel free to create a GitHub issue. Note that for binary data written by
`Externalizable.writeExternal` and the special `writeObject` method the generated code writes it as one byte array
because the serialization data includes no information about the data types. If you want to modify this data in the
generated code, it is recommended to have a look at the source code of the corresponding serialized class (if available)
to determine how its `writeExternal` respectively `writeObject` method is implemented.

Note that this project is not recommended for visualizing the serialization data structure due to the limitations
mentioned above. Instead, other tools such as [NickstaDB's SerializationDumper](https://github.com/NickstaDB/SerializationDumper)
should be used for that.

## Usage
Requires Java 17 or newer

:warning: Code generation should not be performed for untrusted serialization data. Malicious serialization data might
be able to cause a denial of service, or might be able to manipulate the generated code to perform malicious actions.

### Command line
```
java -jar serial-builder-codegen.jar simple-api <serial-data-hex>
```

For example:
```
java -jar serial-builder-codegen.jar simple-api aced0005737200116a6176612e6c616e672e426f6f6c65616ecd207280d59cfaee0200015a000576616c7565787001
```

Produces the output:
```java
byte[] serialData = SimpleSerialBuilder.startSerializableObject()
    .beginClassData("java.lang.Boolean", -3665804199014368530L)
        .primitiveBooleanField("value", true)
    .endClassData()
.endObject();
```

### Programmatically
Code generation can be performed programmatically by using the method `marcono1234.serialization.serialbuilder.codegen.SimpleSerialBuilderCodeGen.generateCode(byte[])`.

Currently this library is not published to Maven Central. You can either [build the project locally](/README.md#building)
or you can [use JitPack as Maven repository](https://jitpack.io/#Marcono1234/serial-builder) serving this library.

When using JitPack it is recommended to put the jitpack.io repository last in the list of declared repositories for
better performance and to avoid pulling undesired dependencies from it. When using Gradle as build tool you should also
use [repository content filtering](https://docs.gradle.org/current/userguide/declaring_repositories.html#sec:repository-content-filtering):
```kotlin
repositories {
    mavenCentral()
    exclusiveContent {
        forRepository {
            maven {
                url = uri("https://jitpack.io")
            }
        }
        filter {
            // Only use JitPack for the `serial-builder-codegen` library
            includeModule("com.github.Marcono1234.serial-builder", "serial-builder-codegen")
        }
    }
}
```
