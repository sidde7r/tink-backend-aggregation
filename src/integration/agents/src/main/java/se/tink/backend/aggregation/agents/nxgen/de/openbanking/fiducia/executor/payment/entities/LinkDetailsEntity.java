package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinkDetailsEntity {
    private String href;

    @JsonIgnore
    public String getHref() {
        return href;
    }
}
