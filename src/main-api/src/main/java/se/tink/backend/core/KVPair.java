package se.tink.backend.core;

import java.util.Objects;

public class KVPair<K, V> {
    private K key;
    private V value;

    public KVPair() {
    }

    public KVPair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public V setValue(V value) {
        this.value = value;
        return this.value;
    }

    @Override
    public String toString() {
        return key + ":" + value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }

        @SuppressWarnings("unchecked")
        final KVPair<K, V> other = (KVPair<K, V>) obj;

        return (Objects.equals(this.key, other.key) && Objects.equals(this.value, other.value));
    }
}
