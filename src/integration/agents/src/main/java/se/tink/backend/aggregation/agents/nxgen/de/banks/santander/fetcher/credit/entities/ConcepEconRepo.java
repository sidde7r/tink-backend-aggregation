package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.credit.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConcepEconRepo {

    @JsonProperty("EMPRESA")
    private String eMPRESA;

    @JsonProperty("CODIGO_NUMERICO")
    private int cODIGONUMERICO;
}
