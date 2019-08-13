package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinkEntity {
    @JsonProperty private String href;

    @JsonIgnore
    public String getHref() {
        return href;
    }
}
