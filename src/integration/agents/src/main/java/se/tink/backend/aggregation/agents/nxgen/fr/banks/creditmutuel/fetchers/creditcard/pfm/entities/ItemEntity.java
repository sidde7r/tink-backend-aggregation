package se.tink.backend.aggregation.agents.nxgen.fr.banks.creditmutuel.fetchers.creditcard.pfm.entities;

import com.fasterxml.jackson.annotation.JsonRootName;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonRootName("item")
@JsonObject
public class ItemEntity {
    private String layout;
    private LinkEntity link;

    private OutputsEntity outputs;
    private String template;

    public OutputsEntity getOutputs() {
        return outputs;
    }
}
