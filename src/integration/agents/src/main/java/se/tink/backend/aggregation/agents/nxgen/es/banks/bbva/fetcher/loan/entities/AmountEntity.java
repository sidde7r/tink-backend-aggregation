package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vavr.control.Option;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.Defaults;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AmountEntity {
    private double amount;
    private String currency;

    public double getAmount() {
        return amount;
    }

    public Option<String> getCurrency() {
        return Option.of(currency);
    }

    @JsonIgnore
    public Amount toTinkAmount() {
        return new Amount(Option.of(currency).getOrElse(Defaults.CURRENCY), amount);
    }
}
