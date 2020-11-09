package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.entity;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class Balance {

    private AmountEntity balanceAmount;
    @Getter private String balanceType;

    public ExactCurrencyAmount toTinkAmount() {
        return balanceAmount.toTinkAmount();
    }
}
