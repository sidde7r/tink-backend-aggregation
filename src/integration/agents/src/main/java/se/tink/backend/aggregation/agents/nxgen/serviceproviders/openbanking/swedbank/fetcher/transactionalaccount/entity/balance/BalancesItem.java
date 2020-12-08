package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.entity.balance;

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
}
