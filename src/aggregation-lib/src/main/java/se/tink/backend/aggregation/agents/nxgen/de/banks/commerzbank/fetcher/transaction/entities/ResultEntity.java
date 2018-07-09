package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ResultEntity {
    private List<ItemsEntity> items;

    public List<ItemsEntity> getItems() {
        return items;
    }
    // `metaData` is null - cannot define it!
}
