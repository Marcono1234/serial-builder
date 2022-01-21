package marcono1234.serialization.serialbuilder.codegen.implementation.streamdata.descriptor;

import java.io.DataInput;
import java.io.IOException;

public record PrimitiveField(String name, PrimitiveFieldType type) {
    public enum PrimitiveFieldType {
        BYTE(Byte.BYTES, DataInput::readByte),
        CHAR(Character.BYTES, DataInput::readChar),
        DOUBLE(Double.BYTES, DataInput::readDouble),
        FLOAT(Float.BYTES, DataInput::readFloat),
        INT(Integer.BYTES, DataInput::readInt),
        LONG(Long.BYTES, DataInput::readLong),
        SHORT(Short.BYTES, DataInput::readShort),
        BOOLEAN(1, DataInput::readBoolean);

        @FunctionalInterface
        private interface ReaderFunction {
            Object readValue(DataInput in) throws IOException;
        }

        private final int bytesCount;
        private final PrimitiveFieldType.ReaderFunction readerFunction;

        PrimitiveFieldType(int bytesCount, PrimitiveFieldType.ReaderFunction readerFunction) {
            this.bytesCount = bytesCount;
            this.readerFunction = readerFunction;
        }

        public int getValueBytesCount() {
            return bytesCount;
        }

        public Object readValue(DataInput dataIn) throws IOException {
            return readerFunction.readValue(dataIn);
        }
    }
}
