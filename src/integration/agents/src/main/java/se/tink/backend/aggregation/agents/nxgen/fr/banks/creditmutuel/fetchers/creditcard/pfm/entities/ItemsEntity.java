package se.tink.backend.aggregation.agents.nxgen.fr.banks.creditmutuel.fetchers.creditcard.pfm.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ItemsEntity {
    private List<ActionEntity> actions;
    private String layout;

    private OutputsEntity outputs;
    private SubItemsEntity subItems;

    public OutputsEntity getOutput() {
        return outputs;
    }

    public SubItemsEntity getSubItems() {
        return subItems;
    }
}
