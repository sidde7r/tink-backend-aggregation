package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalanceEntity {
    @JsonProperty("Disponivel")
    private double available;

    @JsonProperty("Cativo")
    private double captive;

    @JsonProperty("Contabilistico")
    private double accounting;

    @JsonProperty("Autorizado")
    private double authorized;

    @JsonProperty("Descoberto")
    private double uncovered;

    @JsonProperty("Moeda")
    private String currency;

    public double getAccounting() {
        return accounting;
    }

    public String getCurrency() {
        return currency;
    }
}
