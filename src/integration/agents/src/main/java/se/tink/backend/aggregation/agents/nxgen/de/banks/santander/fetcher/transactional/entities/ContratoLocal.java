package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.transactional.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ContratoLocal {

    @JsonProperty("TIPO_CONTRATO_LOCAL")
    private String tIPOCONTRATOLOCAL;

    @JsonProperty("DETALLE_CONTRATO_LOCAL")
    private String dETALLECONTRATOLOCAL;
}
