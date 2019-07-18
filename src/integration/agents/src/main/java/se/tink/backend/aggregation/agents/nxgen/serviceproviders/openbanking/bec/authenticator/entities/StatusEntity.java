package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class StatusEntity {

    @JsonProperty("href")
    private String href;

    public String getHref() {
        return href;
    }
}
