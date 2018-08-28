package se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.core.Amount;

@JsonObject
public class AmountEntity {

    @JsonProperty("posDecimale")
    private int scale;
    @JsonProperty("devise")
    private String currency;
    @JsonProperty("valeur")
    private int unscaledValue;

    private transient Amount amount = null;

    public Amount toTinkAmount() {
        if (amount == null) {
            amount = new Amount(currency, BigDecimal.valueOf(unscaledValue, scale).doubleValue());
        }
        return amount;
    }

}
