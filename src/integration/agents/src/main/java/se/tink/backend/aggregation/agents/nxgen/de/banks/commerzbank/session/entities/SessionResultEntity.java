package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.session.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SessionResultEntity {
    private Object metaData;

    @JsonProperty("items")
    private List<SessionEntity> items;

    public List<SessionEntity> getItems() {
        return items;
    }
}
