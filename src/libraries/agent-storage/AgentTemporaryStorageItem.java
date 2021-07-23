package se.tink.backend.aggregation.nxgen.storage;

import java.util.function.Consumer;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Builder
@Slf4j
public class AgentTemporaryStorageItem<T> {

    private final T item;
    private final Consumer<T> itemCleaner;

    public void clean() {
        itemCleaner.accept(item);
    }
}
