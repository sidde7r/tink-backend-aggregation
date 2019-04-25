package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.fetcher.transactionalaccount.entities;

import org.apache.commons.lang.StringUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class BalanceEntity {

    private BalanceAmountEntity balanceAmount;
    private String balanceType;

    public boolean isAvailableBalance() {
        return StringUtils.equalsIgnoreCase(balanceType, "interimAvailable");
    }

    public Amount getAmount() {
        return balanceAmount.toAmount();
    }
}
