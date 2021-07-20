package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.identitydata.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.identitydata.entities.EvoBancoClientDataEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class EvoBancoIdentityDataResponse {
    @JsonProperty("EE_O_ConsultaPersonaAmpliadaBE")
    private EvoBancoClientDataEntity clientData;
}
