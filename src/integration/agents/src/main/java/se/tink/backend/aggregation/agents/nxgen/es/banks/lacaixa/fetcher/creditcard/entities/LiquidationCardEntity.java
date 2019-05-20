package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LiquidationCardEntity {
    @JsonProperty("numeroTarjeta")
    private String cardNumber;

    @JsonProperty("importeDispuesto")
    private double amountArranged;
}
