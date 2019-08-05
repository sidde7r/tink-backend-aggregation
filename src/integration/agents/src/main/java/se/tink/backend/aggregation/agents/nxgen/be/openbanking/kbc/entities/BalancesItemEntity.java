package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class BalancesItemEntity {

    @JsonProperty("balanceType")
    private String balanceType;

    @JsonProperty("balanceAmount")
    private BalanceAmountEntity balanceAmountEntity;

    public String getBalanceType() {
        return balanceType;
    }

    public ExactCurrencyAmount getBalanceAmountEntity() {
        return new ExactCurrencyAmount(
                balanceAmountEntity.getAmount(), balanceAmountEntity.getCurrency());
    }
}
