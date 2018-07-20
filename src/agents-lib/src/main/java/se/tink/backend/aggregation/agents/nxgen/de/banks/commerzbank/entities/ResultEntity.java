package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.entities;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.account.entities.ItemsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ResultEntity {
    private Object metaData;
    private List<ItemsEntity> items;

    public List<ItemsEntity> getItems() {
        return items;
    }

    public Object getMetaData() {
        return metaData;
    }
}
