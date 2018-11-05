package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SaldoContNatural {
    @JsonProperty("divisa")
    private String divisa;

    @JsonProperty("importe")
    private Object importe;

    public String getDivisa() {
        return divisa;
    }

    public Object getImporte() {
        return importe;
    }
}
