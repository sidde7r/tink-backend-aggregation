package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinkEntity {
    private String href;
    @JsonProperty("verbs")
    private List<String> methods;

    public String getHref() {
        return href;
    }

    public List<String> getMethods() {
        return methods;
    }
}
