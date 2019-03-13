package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vavr.control.Option;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.Defaults;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AmountEntity {
    private double amount;
    private CurrencyEntity currency;

    public double getAmount() {
        return amount;
    }

    public Option<CurrencyEntity> getCurrency() {
        return Option.of(currency);
    }

    @JsonIgnore
    public Amount toTinkAmount() {
        final String currency =
                getCurrency().map(CurrencyEntity::getId).getOrElse(Defaults.CURRENCY);

        return new Amount(currency, amount);
    }
}
