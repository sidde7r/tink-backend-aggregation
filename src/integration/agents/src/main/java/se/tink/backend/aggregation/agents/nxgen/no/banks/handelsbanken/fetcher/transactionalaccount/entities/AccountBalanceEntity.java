package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountBalanceEntity {
    private double availableBalance;
    private double accountingBalance;
    private double valueDatedBalance;
    private double creditLine;
    private double blockedAmount;

    public double getAvailableBalance() {
        return availableBalance;
    }

    public double getAccountingBalance() {
        return accountingBalance;
    }

    public double getValueDatedBalance() {
        return valueDatedBalance;
    }

    public double getCreditLine() {
        return creditLine;
    }

    public double getBlockedAmount() {
        return blockedAmount;
    }
}
