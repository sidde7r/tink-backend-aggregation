package se.tink.backend.aggregation.agents.nxgen.fr.banks.creditmutuel.fetchers.creditcard.pfm.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NoNameEntity {
    private List<ItemsEntity> items;

    public List<ItemsEntity> getItems() {
        return items;
    }
}
