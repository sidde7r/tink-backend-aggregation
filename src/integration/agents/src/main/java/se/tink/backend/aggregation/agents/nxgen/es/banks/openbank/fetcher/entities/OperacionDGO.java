package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OperacionDGO {
    @JsonProperty("centro")
    private CentroEntity centro;

    @JsonProperty("codigoterminaldgo")
    private String codigoterminaldgo;

    @JsonProperty("numerodgo")
    private int numerodgo;

    public CentroEntity getCentro() {
        return centro;
    }

    public String getCodigoterminaldgo() {
        return codigoterminaldgo;
    }

    public int getNumerodgo() {
        return numerodgo;
    }
}
