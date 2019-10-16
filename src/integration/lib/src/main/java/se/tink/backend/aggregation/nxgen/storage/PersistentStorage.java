package se.tink.backend.aggregation.nxgen.storage;

public class PersistentStorage extends Storage {
    private static final String OLD_VALUE_PREFIX = "OLD_";

    @Override
    public void clear() {
        // do not allow to clear the storage
    }

    /**
     * Stores the previous value of the given key instead of removing it entirely. This is meant to
     * be used for sensitive values in persistent storaga that we want to be able to mask out from
     * logging etc. These values update during the course of a refresh, making it necessary to keep
     * the old and new value to be able to mask both of them from the logs.
     *
     * @param key
     * @param newValue
     */
    public void rotateStorageValue(String key, Object newValue) {
        String oldValue = this.get(key);
        this.put(OLD_VALUE_PREFIX + key, oldValue);
        this.put(key, newValue);
    }
}
