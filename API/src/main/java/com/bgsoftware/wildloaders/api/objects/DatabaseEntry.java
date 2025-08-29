package com.bgsoftware.wildloaders.api.objects;

import java.util.Map;
import java.util.Objects;

/**
 * An immutable implementation of {@link Map.Entry} representing a String→Object pair.
 * <p>
 * This class is useful for returning two related values, such as a mapping or association,
 * without creating a full map structure. The {@code DatabaseEntry} is immutable and does not support
 * the {@link #setValue(Object)} operation.
 */
public final class DatabaseEntry implements Map.Entry<String, Object> {

    /**
     * The key of the entry.
     */
    private final String key;

    /**
     * The value associated with the key.
     */
    private final Object value;

    /**
     * Constructs a new {@code DatabaseEntry} with the specified key and value.
     *
     * @param key   the key element of the entry
     * @param value the value element of the entry
     */
    public DatabaseEntry(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Returns the key of this entry.
     *
     * @return the key of the entry
     */
    @Override
    public String getKey() {
        return key;
    }

    /**
     * Returns the value of this entry.
     *
     * @return the value associated with the key
     */
    @Override
    public Object getValue() {
        return value;
    }

    /**
     * Unsupported operation. This implementation is immutable and does not support modification.
     *
     * @param value the new value
     * @return never returns normally
     * @throws UnsupportedOperationException always thrown to indicate the operation is unsupported
     */
    @Override
    public Object setValue(Object value) {
        throw new UnsupportedOperationException("Cannot use the setValue method of DatabaseEntry");
    }

    /**
     * Returns a string representation of this entry.
     *
     * @return a string in the format {@code DatabaseEntry{key=value}}
     */
    @Override
    public String toString() {
        return "DatabaseEntry{" + key + "=" + value + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DatabaseEntry)) return false;
        DatabaseEntry that = (DatabaseEntry) o;
        return Objects.equals(key, that.key) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }
}
