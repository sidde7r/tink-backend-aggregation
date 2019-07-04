package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.entity.balance;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalancesItem {

    private String balanceType;

    private BalanceAmount balanceAmount;

    private String referenceDate;

    public String getBalanceType() {
        return balanceType;
    }

    public BalanceAmount getBalanceAmount() {
        return balanceAmount;
    }

    public String getReferenceDate() {
        return referenceDate;
    }

    @Override
    public String toString() {
        return "BalancesItem{"
                + "balanceType = '"
                + balanceType
                + '\''
                + ",balanceAmount = '"
                + balanceAmount
                + '\''
                + ",referenceDate = '"
                + referenceDate
                + '\''
                + "}";
    }
}
