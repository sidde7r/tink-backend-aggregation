package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.identitydata.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class EvoBancoIdentityEntity {

    @JsonProperty("DatosPersonales")
    private EvoBancoIdentityPersonalEntity identityPersonal;
}
