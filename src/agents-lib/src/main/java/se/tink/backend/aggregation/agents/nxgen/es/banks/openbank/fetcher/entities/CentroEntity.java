package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CentroEntity {
    @JsonProperty("centro")
    private String centro;

    @JsonProperty("empresa")
    private String empresa;

    public String getCentro() {
        return centro;
    }

    public String getEmpresa() {
        return empresa;
    }
}
