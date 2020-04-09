package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants.Accounts;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class BalanceEntity {
    private BalanceAmountEntity balanceAmount;
    private String balanceType;
    private Boolean creditLimitIncluded;
    private String lastChangeDateTime;
    private String lastCommittedTransaction;
    private String referenceDate;

    public ExactCurrencyAmount toAmonut() {
        return balanceAmount.toAmount();
    }

    public boolean isClosingBooked() {
        return balanceType.equalsIgnoreCase(Accounts.BALANCE_CLOSING_BOOKED)
                || balanceType.equalsIgnoreCase("interimAvailable");
    }
}
