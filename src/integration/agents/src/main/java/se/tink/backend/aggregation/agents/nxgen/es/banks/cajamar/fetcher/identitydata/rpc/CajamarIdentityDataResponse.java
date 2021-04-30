package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.identitydata.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class CajamarIdentityDataResponse {
    @JsonProperty("PDF")
    private String pdf;
}
