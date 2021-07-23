package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Getter
public class BalanceEntity {

    private AmountEntity balanceAmount;
    private String balanceType;
    private Boolean creditLimitincluded;

    @JsonIgnore
    public boolean isAvailableCredit() {
        return SebCommonConstants.Accounts.AVAILABLE_BALANCE.equalsIgnoreCase(balanceType)
                || SebCommonConstants.Accounts.AVAILABLE_BALANCE_MISSPELLED.equalsIgnoreCase(
                        balanceType);
    }

    @JsonIgnore
    public boolean isBalance() {
        return SebCommonConstants.Accounts.AVAILABLE_BALANCE_EXPECTED.equalsIgnoreCase(balanceType);
    }

    @JsonIgnore
    public ExactCurrencyAmount toAmount() {
        return balanceAmount.toTinkAmount();
    }
}
