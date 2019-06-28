package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LabelUserEntity {
    @JsonProperty("display_name")
    private String displayName;

    public LabelUserEntity(String displayName) {
        this.displayName = displayName;
    }
}
