package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.transactionalaccount.entitites;

import java.util.Objects;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountBalanceEntity {
    private Double availableBalance;
    private Double accountingBalance;
    private Double valueDatedBalance;
    private Double creditLine;
    private Double blockedAmount;

    public Double getAvailableBalance() {
        return availableBalance;
    }

    public Double getAccountingBalance() {
        return accountingBalance;
    }

    public boolean isAvailableBalanceNull() {
        return Objects.isNull(availableBalance);
    }

    public Double getValueDatedBalance() {
        return valueDatedBalance;
    }

    public Double getCreditLine() {
        return creditLine;
    }

    public Double getBlockedAmount() {
        return blockedAmount;
    }
}
