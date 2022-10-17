package marcono1234.serialization.serialbuilder.simplebuilder2.classdata;

import marcono1234.serialization.serialbuilder.simplebuilder.api.object.serializable.SerializableObjectData;
import marcono1234.serialization.serialbuilder.simplebuilder2.SerializedField;

public sealed interface PrimitiveField extends SerializedField {
    record BooleanField(String fieldName, boolean value) implements PrimitiveField {
        @Override
        public <C> SerializableObjectData<C> buildSerialData(SerializableObjectData<C> start) {
            return start.primitiveBooleanField(fieldName, value);
        }
    }

    record ByteField(String fieldName, byte value) implements PrimitiveField {
        @Override
        public <C> SerializableObjectData<C> buildSerialData(SerializableObjectData<C> start) {
            return start.primitiveByteField(fieldName, value);
        }
    }

    record CharField(String fieldName, char value) implements PrimitiveField {
        @Override
        public <C> SerializableObjectData<C> buildSerialData(SerializableObjectData<C> start) {
            return start.primitiveCharField(fieldName, value);
        }
    }

    record ShortField(String fieldName, short value) implements PrimitiveField {
        @Override
        public <C> SerializableObjectData<C> buildSerialData(SerializableObjectData<C> start) {
            return start.primitiveShortField(fieldName, value);
        }
    }

    record IntField(String fieldName, int value) implements PrimitiveField {
        @Override
        public <C> SerializableObjectData<C> buildSerialData(SerializableObjectData<C> start) {
            return start.primitiveIntField(fieldName, value);
        }
    }

    record LongField(String fieldName, long value) implements PrimitiveField {
        @Override
        public <C> SerializableObjectData<C> buildSerialData(SerializableObjectData<C> start) {
            return start.primitiveLongField(fieldName, value);
        }
    }

    record FloatField(String fieldName, float value) implements PrimitiveField {
        @Override
        public <C> SerializableObjectData<C> buildSerialData(SerializableObjectData<C> start) {
            return start.primitiveFloatField(fieldName, value);
        }
    }

    record DoubleField(String fieldName, double value) implements PrimitiveField {
        @Override
        public <C> SerializableObjectData<C> buildSerialData(SerializableObjectData<C> start) {
            return start.primitiveDoubleField(fieldName, value);
        }
    }
}
