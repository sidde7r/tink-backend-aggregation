package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.entities.StatusEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SpankkiResponse {
    @JsonProperty private StatusEntity status;

    @JsonIgnore
    public StatusEntity getStatus() {
        return status;
    }
}
