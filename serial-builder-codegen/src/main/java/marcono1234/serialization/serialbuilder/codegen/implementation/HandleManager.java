package marcono1234.serialization.serialbuilder.codegen.implementation;

import marcono1234.serialization.serialbuilder.codegen.implementation.streamdata.StreamObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class HandleManager {
    public static class Handle<T extends StreamObject> implements StreamObject {
        private final Class<? extends T> objectType;
        private T object;
        private boolean isUsed = false;

        @SuppressWarnings("unchecked")
        private Handle(T object) {
            this((Class<? extends T>) object.getClass());
            this.object = object;
        }

        private Handle(Class<? extends T> objectType) {
            this.objectType = Objects.requireNonNull(objectType);
            this.object = null;
        }

        public void setObject(T object) {
            if (this.object != null) {
                throw new IllegalStateException("Handle is already assigned");
            }
            if (!objectType.isInstance(object)) {
                throw new IllegalArgumentException("Not an instance of " + objectType);
            }
            this.object = Objects.requireNonNull(object);
        }

        /**
         * Gets the referenced object. Returns an empty {@code Optional} if the object is not
         * completely created yet (indicates a cyclic reference).
         */
        public Optional<StreamObject> getReferencedObject() {
            return Optional.ofNullable(object);
        }

        public Class<? extends StreamObject> getObjectType() {
            return objectType;
        }

        /**
         * Marks the handle as used, that is, it is referenced in the generated code.
         *
         * @see #isUsed()
         */
        public void markUsed() {
            isUsed = true;
        }

        /**
         * @return whether this handle is referenced in the generated code
         */
        public boolean isUsed() {
            return isUsed;
        }
    }

    private final List<Handle<?>> handles = new ArrayList<>();

    public void createAssignedHandle(StreamObject object) {
        handles.add(new Handle<>(object));
    }

    public <T extends StreamObject> Handle<T> newUnassignedHandle(Class<? extends T> objectType) {
        Handle<T> handle = new Handle<>(objectType);
        handles.add(handle);
        return handle;
    }

    public Optional<Handle<?>> getHandle(int handleIndex) {
        if (handleIndex < 0 || handleIndex >= handles.size()) {
            return Optional.empty();
        }
        return Optional.of(handles.get(handleIndex));
    }

    private static String getHandleName(int index) {
        return "handle" + (index + 1);
    }

    /**
     * Gets the variable name for a {@linkplain Handle#isUsed() used handle}.
     *
     * @see #getUsedHandleNames()
     */
    public String getHandleName(Handle<?> handle) {
        if (!handle.isUsed()) {
            throw new IllegalArgumentException("Cannot be name for unused handle");
        }

        int nextIndex = 0;
        for (Handle<?> currentHandle : handles) {
            if (currentHandle.equals(handle)) {
                return getHandleName(nextIndex);
            } else if (currentHandle.isUsed()) {
                nextIndex++;
            }
        }
        throw new IllegalArgumentException("Handle is not tracked by this manager");
    }

    /**
     * Gets the {@linkplain #getHandleName(Handle) variable names} of all handles which
     * are referenced by the generated code.
     */
    public List<String> getUsedHandleNames() {
        List<String> names = new ArrayList<>();

        int nextIndex = 0;
        for (Handle<?> handle : handles) {
            if (handle.isUsed()) {
                names.add(getHandleName(nextIndex));
                nextIndex++;
            }
        }

        return names;
    }
}
