package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@SuppressWarnings({"unused", "FieldCanBeLocal"})
@JsonObject
public class FetchIdentityRequest {
    @JsonProperty("entidad")
    private int entity;

    @JsonProperty("oficina")
    private int office;

    public FetchIdentityRequest(int entity, int office) {
        this.entity = entity;
        this.office = office;
    }
}
