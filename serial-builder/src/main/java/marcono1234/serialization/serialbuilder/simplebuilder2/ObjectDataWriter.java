package marcono1234.serialization.serialbuilder.simplebuilder2;

import marcono1234.serialization.serialbuilder.simplebuilder.api.ObjectBuildingDataOutput;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

public class ObjectDataWriter implements DataOutput {
    private final ObjectBuildingDataOutput delegate;

    public ObjectDataWriter(ObjectBuildingDataOutput delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    public void object(ObjectBase object) {
        object.buildSerialData(delegate);
    }

    @Override
    public void write(int b) throws IOException {
        delegate.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        delegate.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        delegate.write(b, off, len);
    }

    @Override
    public void writeBoolean(boolean v) throws IOException {
        delegate.writeBoolean(v);
    }

    @Override
    public void writeByte(int v) throws IOException {
        delegate.writeByte(v);
    }

    @Override
    public void writeShort(int v) throws IOException {
        delegate.writeShort(v);
    }

    @Override
    public void writeChar(int v) throws IOException {
        delegate.writeChar(v);
    }

    @Override
    public void writeInt(int v) throws IOException {
        delegate.writeInt(v);
    }

    @Override
    public void writeLong(long v) throws IOException {
        delegate.writeLong(v);
    }

    @Override
    public void writeFloat(float v) throws IOException {
        delegate.writeFloat(v);
    }

    @Override
    public void writeDouble(double v) throws IOException {
        delegate.writeDouble(v);
    }

    @Override
    public void writeChars(String s) throws IOException {
        delegate.writeChars(s);
    }

    @Override
    public void writeUTF(String s) throws IOException {
        delegate.writeUTF(s);
    }

    /**
     * @deprecated
     *      This method is error-prone because it discards the high 8 bits of each char.
     */
    @Deprecated
    @Override
    public void writeBytes(String s) throws IOException {
        delegate.writeBytes(s);
    }
}
