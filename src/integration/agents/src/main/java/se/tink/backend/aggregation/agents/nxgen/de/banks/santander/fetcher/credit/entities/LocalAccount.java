package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.credit.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LocalAccount {

    @JsonProperty("TIPO_CONTRATO_LOCAL")
    private Object tIPOCONTRATOLOCAL;

    @JsonProperty("DETALLE_CONTRATO_LOCAL")
    private Object dETALLECONTRATOLOCAL;
}
