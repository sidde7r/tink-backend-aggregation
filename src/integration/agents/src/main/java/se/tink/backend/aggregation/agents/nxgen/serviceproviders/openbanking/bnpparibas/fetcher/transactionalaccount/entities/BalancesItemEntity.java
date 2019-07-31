package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class BalancesItemEntity {

    private String balanceType;

    private String name;

    private String lastCommittedTransaction;

    @JsonProperty("balanceAmount")
    private BalanceAmountEntity balanceAmountEntity;

    public String getBalanceType() {
        return balanceType;
    }

    public String getName() {
        return name;
    }

    public String getLastCommittedTransaction() {
        return lastCommittedTransaction;
    }

    public BalanceAmountEntity getBalanceAmountEntity() {
        return balanceAmountEntity;
    }

    public boolean isAvailableBalance() {
        return balanceType.equalsIgnoreCase("OTHR");
    }

    public ExactCurrencyAmount toAmount() {
        return new ExactCurrencyAmount(
                balanceAmountEntity.getAmount(), balanceAmountEntity.getCurrency());
    }
}
