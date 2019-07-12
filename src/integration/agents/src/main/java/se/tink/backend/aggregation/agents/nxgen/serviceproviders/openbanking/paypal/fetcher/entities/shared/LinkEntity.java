package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher.entities.shared;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinkEntity {

    @JsonProperty("href")
    private String reference;

    @JsonProperty("rel")
    private String relation;

    private String method;

    public String getReference() {
        return reference;
    }

    public String getRelation() {
        return relation;
    }
}
