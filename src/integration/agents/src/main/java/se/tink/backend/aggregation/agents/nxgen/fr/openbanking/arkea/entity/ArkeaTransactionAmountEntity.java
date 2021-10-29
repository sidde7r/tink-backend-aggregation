package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Data
@JsonObject
public class ArkeaTransactionAmountEntity {

    private String currency;
    private BigDecimal amount;

    @JsonIgnore
    public ExactCurrencyAmount getAmount(String creditDebitIndicator) {
        return creditDebitIndicator.equals("DBIT")
                ? new ExactCurrencyAmount(amount.negate(), currency)
                : new ExactCurrencyAmount(amount, currency);
    }
}
