package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinkEntity {

    @JsonProperty("href")
    private String url;

    public String getUrl() {
        return url;
    }
}
