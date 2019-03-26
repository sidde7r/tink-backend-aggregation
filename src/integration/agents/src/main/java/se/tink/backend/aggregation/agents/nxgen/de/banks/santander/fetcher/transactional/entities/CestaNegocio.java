package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.transactional.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CestaNegocio {

    @JsonProperty("CODIGO_AGRUPACION")
    private String cODIGOAGRUPACION;

    @JsonProperty("EMPRESA")
    private String eMPRESA;
}
