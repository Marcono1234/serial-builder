package marcono1234.serialization.serialbuilder;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import static marcono1234.serialization.serialbuilder.simplebuilder2.FactoryMethods.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

// TODO: Use proper test class name
public class V2Test {
    private static <T> T deserialize(byte[] data) {
        try (ObjectInputStream objIn = new ObjectInputStream(new ByteArrayInputStream(data))) {
            @SuppressWarnings("unchecked")
            T result = (T) objIn.readObject();
            return result;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static class SerializableClass implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        public int i;
        public int[] array;
        public String s;
    }

    @Test
    void test() {
        byte[] b = serializableObject(
            classData(
                SerializableClass.class,
                List.of(
                    intField("i", 6),
                    objectField(
                        "array",
                        int[].class,
                        array(new int[] {1, 2, 3})
                    ),
                    objectField(
                        "s",
                        String.class,
                        string("nested-test")
                    )
                )
            )
        ).createSerialData();

        SerializableClass actualObject = deserialize(b);
        assertEquals(6, actualObject.i);
        assertArrayEquals(new int[] {1, 2, 3}, actualObject.array);
        assertEquals("nested-test", actualObject.s);
    }
}
