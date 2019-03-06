package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AmountEntity {
    private double amount;
    private CurrencyEntity currency;

    public double getAmount() {
        return amount;
    }

    public CurrencyEntity getCurrency() {
        return currency;
    }

    @JsonIgnore
    public Amount toTinkAmount() {
        final String currency =
                getCurrency() != null && getCurrency().getId() != null
                        ? getCurrency().getId()
                        : BbvaConstants.Defaults.CURRENCY;

        return new Amount(currency, amount);
    }
}
