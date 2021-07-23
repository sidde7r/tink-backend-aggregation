package se.tink.backend.aggregation.nxgen.storage;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.utils.log.LogTag;

@Slf4j
public class AgentTemporaryStorageImpl implements AgentTemporaryStorage {

    private static final LogTag LOG_TAG = LogTag.from("[AgentTemporaryStorage]");

    private final Map<String, AgentTemporaryStorageItem<?>> itemsMap = new HashMap<>();

    @Override
    public void save(String itemKey, AgentTemporaryStorageItem<?> item) {
        if (itemsMap.containsKey(itemKey)) {
            log.warn("{} Removing existing item with key: {}", LOG_TAG, itemKey);
            remove(itemKey);
        }

        log.info("{} Adding item with key: {}", LOG_TAG, itemKey);
        itemsMap.put(itemKey, item);
    }

    @Override
    public <T> T get(String itemKey, Class<T> itemClass) {
        return Optional.ofNullable(itemsMap.get(itemKey))
                .map(AgentTemporaryStorageItem::getItem)
                .map(itemClass::cast)
                .orElseThrow(() -> new IllegalStateException("No item found with key: " + itemKey));
    }

    @Override
    public void remove(String itemKey) {
        if (!itemsMap.containsKey(itemKey)) {
            log.error("{} Could not find item to remove: {}", LOG_TAG, itemKey);
            return;
        }

        cleanItem(itemKey);
        log.info("{} Removing item with key: {}", LOG_TAG, itemKey);
        itemsMap.remove(itemKey);
    }

    private void cleanItem(String itemKey) {
        log.info("{} Cleaning item: {}", LOG_TAG, itemKey);
        try {
            itemsMap.get(itemKey).clean();
        } catch (Exception e) {
            log.error("{} Error when cleaning item: {}", LOG_TAG, itemKey, e);
        }
    }

    @Override
    public void clear() {
        log.info("{} Clearing whole storage. Items: {}", LOG_TAG, itemsMap.keySet().size());
        for (String key : itemsMap.keySet()) {
            remove(key);
        }
    }
}
