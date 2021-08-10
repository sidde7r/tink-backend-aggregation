package se.tink.backend.aggregation.nxgen.storage;

/**
 * Temporary storage available for the whole lifetime of an agent. It should contain items that
 * always have to be cleaned after agent operation execution is finished.
 */
public interface AgentTemporaryStorage {

    void save(String itemKey, AgentTemporaryStorageItem<?> item);

    <T> T get(String itemKey, Class<T> itemClass);

    void remove(String itemKey);

    void clear();
}
