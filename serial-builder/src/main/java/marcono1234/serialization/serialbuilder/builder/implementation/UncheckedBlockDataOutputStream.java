package marcono1234.serialization.serialbuilder.builder.implementation;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.Objects;

import static java.io.ObjectStreamConstants.TC_BLOCKDATA;
import static java.io.ObjectStreamConstants.TC_BLOCKDATALONG;
import static java.io.ObjectStreamConstants.TC_LONGSTRING;
import static java.io.ObjectStreamConstants.TC_STRING;

/*
 * TODO: Write primitive data in 1024 byte block chunks?
 * However, this is not a requirement by the serialization protocol and ObjectInputStream is able to read
 * the data even if it is not grouped in 1024 byte block chunks.
 */

/**
 * {@link DataOutput} implementation which also supports special serialization data methods, such as
 * {@link #setBlockDataMode(boolean)} to enable or disable block mode.
 *
 * <p>All methods of this class wrap thrown checked exceptions in unchecked ones.
 */
class UncheckedBlockDataOutputStream implements DataOutput, Closeable {
    private DataOutputStream dataOut;
    private ByteArrayOutputStream pendingBlockData;
    /**
     * Either {@link #dataOut} or wrapping {@link #pendingBlockData}, depending on whether block
     * data mode is currently active.
     */
    private DataOutputStream currentDataOut;

    public UncheckedBlockDataOutputStream(OutputStream out) {
        this.dataOut = new DataOutputStream(Objects.requireNonNull(out));
        currentDataOut = dataOut;
        pendingBlockData = null;
    }

    @Override
    public void write(int b) {
        try {
            currentDataOut.write(b);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void write(byte[] b) {
        try {
            currentDataOut.write(b);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void write(byte[] b, int off, int len) {
        try {
            currentDataOut.write(b, off, len);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeBoolean(boolean v) {
        try {
            currentDataOut.writeBoolean(v);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeByte(int v) {
        try {
            currentDataOut.writeByte(v);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeShort(int v) {
        try {
            currentDataOut.writeShort(v);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeChar(int v) {
        try {
            currentDataOut.writeChar(v);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeInt(int v) {
        try {
            currentDataOut.writeInt(v);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeLong(long v) {
        try {
            currentDataOut.writeLong(v);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeFloat(float v) {
        try {
            currentDataOut.writeFloat(v);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeDouble(double v) {
        try {
            currentDataOut.writeDouble(v);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Deprecated // Method is error-prone because it only supports ASCII
    @Override
    public void writeBytes(String s) {
        try {
            currentDataOut.writeBytes(s);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeChars(String s) {
        try {
            currentDataOut.writeChars(s);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeUTF(String s) {
        try {
            currentDataOut.writeUTF(s);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // Custom methods

    /**
     * Writes a {@code String} in the serialization format.
     */
    public void writeSerialString(String s) {
        ByteArrayOutputStream tempOut = new ByteArrayOutputStream(s.length());

        // See https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/io/DataInput.html#modified-utf-8
        for (char c : s.toCharArray()) {
            if (c >= 0x0001 && c <= 0x007F) {
                tempOut.write(c);
            }
            // Covers 0x0000 || (0x0080 - 0x07FF)
            else if (c <= 0x07FF) {
                tempOut.write(0b110_00000 | (c >> 6) & 0b11111);
                tempOut.write(0b10_000000 | c & 0b111111);
            }
            // Covers 0x0800 - 0xFFFF
            else {
                tempOut.write(0b1110_0000 | (c >> 12) & 0b1111);
                tempOut.write(0b10_000000 | (c >> 6) & 0b111111);
                tempOut.write(0b10_000000 | c & 0b111111);
            }
        }

        byte[] stringBytes = tempOut.toByteArray();
        int length = stringBytes.length;
        if (length <= 65535) {
            writeByte(TC_STRING);
            writeShort(length);
        } else {
            writeByte(TC_LONGSTRING);
            writeLong(length);
        }
        write(stringBytes);
    }

    public void writeSerialArray(boolean... array) {
        try {
            currentDataOut.writeInt(array.length);
            for (boolean b : array) {
                currentDataOut.writeBoolean(b);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void writeSerialArray(byte... array) {
        try {
            currentDataOut.writeInt(array.length);
            for (byte b : array) {
                currentDataOut.writeByte(b);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void writeSerialArray(char... array) {
        try {
            currentDataOut.writeInt(array.length);
            for (char c : array) {
                currentDataOut.writeChar(c);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void writeSerialArray(short... array) {
        try {
            currentDataOut.writeInt(array.length);
            for (short s : array) {
                currentDataOut.writeShort(s);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void writeSerialArray(int... array) {
        try {
            currentDataOut.writeInt(array.length);
            for (int i : array) {
                currentDataOut.writeInt(i);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void writeSerialArray(long... array) {
        try {
            currentDataOut.writeInt(array.length);
            for (long l : array) {
                currentDataOut.writeLong(l);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void writeSerialArray(float... array) {
        try {
            currentDataOut.writeInt(array.length);
            for (float f : array) {
                currentDataOut.writeFloat(f);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void writeSerialArray(double... array) {
        try {
            currentDataOut.writeInt(array.length);
            for (double d : array) {
                currentDataOut.writeDouble(d);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // Block data methods

    public boolean isBlockDataModeActive() {
        return pendingBlockData != null;
    }

    public boolean setBlockDataMode(boolean active) {
        boolean wasActive = isBlockDataModeActive();

        if (active) {
            if (wasActive) {
                throw new IllegalStateException("Block data mode is already active");
            }
            pendingBlockData = new ByteArrayOutputStream();
            currentDataOut = new DataOutputStream(pendingBlockData);
        } else {
            if (!wasActive) {
                // Nothing to do
                return false;
            }

            try {
                currentDataOut.flush();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            byte[] blockData = pendingBlockData.toByteArray();
            pendingBlockData = null;
            // Restore non-block-mode output
            currentDataOut = dataOut;

            int length = blockData.length;
            if (length > 0) {
                if (length <= 255) {
                    writeByte(TC_BLOCKDATA);
                    writeByte(length);
                } else {
                    writeByte(TC_BLOCKDATALONG);
                    writeInt(length);
                }
                write(blockData);
            }
        }

        return wasActive;
    }

    @Override
    public void close() {
        if (dataOut == null) {
            return;
        }
        try {
            dataOut.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        dataOut = null;

        // Check for pending block data after stream has been closed, in case close() was called as
        // part of exception handling (e.g. try-with-resources)
        if (isBlockDataModeActive()) {
            throw new IllegalStateException("Stream has pending block data");
        }
    }
}
