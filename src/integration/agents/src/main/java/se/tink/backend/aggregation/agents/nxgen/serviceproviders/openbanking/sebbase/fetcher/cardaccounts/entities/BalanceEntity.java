package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class BalanceEntity {

    private BalanceAmountEntity balanceAmount;
    private String balanceType;
    private Boolean creditLimitincluded;

    public BalanceAmountEntity getBalanceAmount() {
        return balanceAmount;
    }

    public String getBalanceType() {
        return balanceType;
    }

    public Boolean getCreditLimitincluded() {
        return creditLimitincluded;
    }

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
        return Optional.ofNullable(balanceAmount)
                .map(b -> new ExactCurrencyAmount(b.getAmount(), b.getCurrency()))
                .orElseThrow(() -> new IllegalStateException("Could not parse amount"));
    }
}
