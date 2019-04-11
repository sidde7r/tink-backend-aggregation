package se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AmountEntity {

    @JsonProperty("posDecimale")
    private int scale;

    @JsonProperty("devise")
    private String currency;

    @JsonProperty("valeur")
    private int unscaledValue;

    public Amount toTinkAmount() {
        return new Amount(currency, BigDecimal.valueOf(unscaledValue, scale).doubleValue());
    }
}
