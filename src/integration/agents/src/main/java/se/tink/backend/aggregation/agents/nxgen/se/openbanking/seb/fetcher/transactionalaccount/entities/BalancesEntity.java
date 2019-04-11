package se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.SebConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class BalancesEntity {
    @JsonIgnore public static Amount Default = Amount.inSEK(0);

    private String balanceType;

    private String creditLimitIncluded;

    private BalanceAmountEntity balanceAmount;

    public boolean isAvailableBalance() {
        return balanceType.equalsIgnoreCase(SebConstants.Accounts.AVAILABLE_BALANCE);
    }

    public Amount toAmount() {
        return balanceAmount != null
                ? new Amount(balanceAmount.getCurrency(), balanceAmount.getAmount())
                : BalancesEntity.Default;
    }
}
