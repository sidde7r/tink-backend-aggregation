package se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.SEBConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class BalancesEntity {
    @JsonIgnore
    public static Amount Default = Amount.inSEK(0);

    @JsonProperty
    private String balanceType;

    @JsonProperty
    private String creditLimitIncluded;

    @JsonProperty
    private BalanceAmountEntity balanceAmount;

    public boolean isAvailableBalance() {
        return balanceType.equalsIgnoreCase(SEBConstants.ACCOUNTS.AVAILABLE_BALANCE);
    }

    public Amount toAmount() {
        return balanceAmount != null ?
                new Amount(balanceAmount.getCurrency(), balanceAmount.getAmount()) :
                BalancesEntity.Default;
    }
}
