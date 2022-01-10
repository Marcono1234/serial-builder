package marcono1234.serialization.serialbuilder.builder.api;

import java.io.IOException;

@FunctionalInterface
public interface ThrowingConsumer<T> {
    void accept(T t) throws IOException;
}
