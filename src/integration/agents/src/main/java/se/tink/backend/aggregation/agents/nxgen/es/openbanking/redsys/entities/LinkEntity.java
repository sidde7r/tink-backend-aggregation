package se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinkEntity {
    @JsonProperty private String href;

    public String getHref() {
        return href;
    }
}
