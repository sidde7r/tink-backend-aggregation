package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.upcomingtransaction.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
class RecipientEntity {
    @JsonProperty("Name")
    private String name;

    @JsonProperty("RecipientNumber")
    private String recipientNumber;

    @JsonProperty("Reference")
    private String reference;

    public String getName() {
        return name;
    }

    public String getRecipientNumber() {
        return recipientNumber;
    }

    public String getReference() {
        return reference;
    }
}
