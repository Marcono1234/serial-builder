:warning: This library is currently experimental, its behavior and API might change in the future.

---

# serial-builder

Library for creating [Java serialization data](https://docs.oracle.com/en/java/javase/17/docs/specs/serialization/index.html);
mainly intended for research purposes. It is not recommended to use it in production as alternative for `ObjectOutputStream`.

Compared to using Java's `ObjectOutputStream` this library has the following advantages:
- It is not necessary to have the target classes on the classpath; it is possible to refer to classes only by their name.
- It is possible to write arbitrary field values without having to access the internals of the target class with reflection.
- It is possible to omit data or add additional serialization data which would normally not be written.

The entrypoints of this library are the classes [`SerialBuilder`](serial-builder/src/main/java/marcono1234/serialization/serialbuilder/SerialBuilder.java)
and [`SimpleSerialBuilder`](serial-builder/src/main/java/marcono1234/serialization/serialbuilder/SimpleSerialBuilder.java).
The API structure of `SerialBuilder` is pretty close to the actual serialization data format. This allows low level
creation of serialization data, at the cost of verbose usage and reduced error checking. `SimpleSerialBuilder` operates
on a higher level, which makes its usage more concise and less error-prone. In most cases the API  offered by
`SimpleSerialBuilder` should suffice.

The API offered by this library uses a 'fluent builder style', where all methods calls are chained after each other
(with indentation to increase readability) until the end of the chain is reached, and the resulting serialization
data in the form of `byte[]` is returned. Using the API in any other way is not supported and might cause exceptions.
It is recommended to follow the IDE code completion suggestions while using the API, looking at the builder API
interfaces is most likely not that helpful.

## Usage
Requires Java 17 or newer

Currently this library is not published to Maven Central. You can either [build the project locally](#building)
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
            // Only use JitPack for the `serial-builder` library
            includeModule("com.github.Marcono1234.serial-builder", "serial-builder")
        }
    }
}
```

## API usage examples (`SimpleSerialBuilder`)

Note: This project also supports generating Java code using this API to recreate existing serialization data, see the
[code generation section below](#code-generation).

### Class hierarchy
Let's assume you have these two classes:
```java
class ClassA implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public String a;
}

class ClassB extends ClassA {
    @Serial
    private static final long serialVersionUID = 1L;

    public String b;
}
```

To create serialization data for an instance of `ClassB`, you can use the API in the following way:
```java
byte[] serialData = SimpleSerialBuilder.startSerializableObject()
    // Start at the superclass
    .beginClassData(ClassA.class)
        .beginObjectField("a", String.class)
            .string("value-a")
        .endField()
    .endClassData()
    .beginClassData(ClassB.class)
        .beginObjectField("b", String.class)
            .string("value-b")
        .endField()
    .endClassData()
.endObject();
```

### `writeObject` method
Let's assume you have the following class with `writeObject` and `readObject` methods:
```java
class ClassWithWriteObject implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public int i;
    public String s;

    public transient int i2;
    public transient String s2;

    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();

        out.writeInt(i2);
        out.writeObject(s2);
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        i2 = in.readInt();
        s2 = (String) in.readObject();
    }
}
```

To create serialization data for it, you can use the API in the following way:
```java
byte[] serialData = SimpleSerialBuilder.startSerializableObject()
    .beginClassData(ClassWithWriteObject.class)
        // Represents the data written by the `defaultWriteObject()` call
        .primitiveField("i", 1)
        .beginObjectField("s", String.class)
            .string("test")
        .endField()
        // Represents the data manually written by `writeObject`
        .writeObjectWith(writer -> {
            writer.writeInt(2);
            writer.string("manually-written");
        })
    .endClassData()
.endObject();
```

### `Proxy` instances
Let's assume you have the following `java.lang.reflect.InvocationHandler` implementation:
```java
class CustomInvocationHandler implements InvocationHandler, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public String result;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        return result;
    }
}
```

To create serialization data for a `java.lang.reflect.Proxy` instance which uses an instance of that invocation handler, you can use the
API in the following way:
```java
// Starts a Proxy object which implements the Callable interface
byte[] serialData = SimpleSerialBuilder.startProxyObject(Callable.class)
    .beginSerializableInvocationHandler()
        .beginClassData(CustomInvocationHandler.class)
            .beginObjectField("result", String.class)
                .string("custom-result")
            .endField()
        .endClassData()
    .endObject()
.endProxyObject();
```

### Handles
The serialization protocol supports _handles_ which refer to a previously written instance. This API supports this
feature through the [`Handle`](serial-builder/src/main/java/marcono1234/serialization/serialbuilder/builder/api/Handle.java) class.
First you create a new (unassigned) `Handle`, then you pass it to one of the builder methods with `Handle` parameter
and afterwards you can use it to refer to the previously written object.

This library does not support using `Handle` in all cases where the serialization protocol supports it, but all
interesting cases should be covered (if you are missing support for a use case, feel free to create a GitHub issue).

Let's assume you have the following class:

```java
class Container implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public Serializable element;
}
```

To create serialization data for an instance of this class which contains itself, you can use the API in the following way:
```java
// First create a new unassigned handle
Handle selfHandle = new Handle();
// Then pass the handle here as argument to assign the written object to it
byte[] serialData = SimpleSerialBuilder.startSerializableObject(selfHandle)
    .beginClassData(Container.class)
        .beginObjectField("element", Serializable.class)
            // Finally, write a reference to the previously written object
            .objectHandle(selfHandle)
        .endField()
    .endClassData()
.endObject();
```

## Code generation
Especially when using this API for existing large serialization data, it can be cumbersome to manually write all the
Java code to recreate the serialization data. Therefore, this project provides code generation functionality which,
for given serialization data, generates the corresponding API calls to recreate the serialization data (as close as
possible). See the [README of the subproject](./serial-builder-codegen/README.md) for more information.

## Project structure
This project is a multi-project Gradle build. It has the following subprojects:
- [`serial-builder`](./serial-builder): Contains the source code of the builder API
- [`serial-builder-codegen`](./serial-builder-codegen): Contains the source code for [code generation](#code-generation)

## Building
This project uses Gradle for building; just run:
```
./gradlew build
```

It is built against Java 17, but there is no need to manually install the correct JDK; Gradle's [toolchain](https://docs.gradle.org/current/userguide/toolchains.html)
feature automatically downloads the needed JDK. Some IDEs do not support toolchains yet, so you might have to
configure them manually.

## Similar / related projects
- [NickstaDB's SerializationDumper](https://github.com/NickstaDB/SerializationDumper)
- [frohoff's ysoserial](https://github.com/frohoff/ysoserial)
- [Moritz Bechler's ruby-serialize](https://github.com/mb-syss/ruby-serialize)

## License
This project [uses the MIT license](./LICENSE.txt); all contributions are implicitly under that license.
