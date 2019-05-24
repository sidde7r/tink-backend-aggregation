
package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.fetcher.cardaccounts.entities;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.SebConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.fetcher.transactionalaccount.entities.BalancesEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

import java.util.Optional;

@JsonObject
public class Balance {

    private BalanceAmount balanceAmount;

    private String balanceType;

    private Boolean creditLimitincluded;

    public BalanceAmount getBalanceAmount() {
        return balanceAmount;
    }

    public void setBalanceAmount(BalanceAmount balanceAmount) {
        this.balanceAmount = balanceAmount;
    }

    public String getBalanceType() {
        return balanceType;
    }

    public void setBalanceType(String balanceType) {
        this.balanceType = balanceType;
    }

    public Boolean getCreditLimitincluded() {
        return creditLimitincluded;
    }

    public void setCreditLimitincluded(Boolean creditLimitincluded) {
        this.creditLimitincluded = creditLimitincluded;
    }

    public boolean isAvailableBalance() {
        return balanceType.equalsIgnoreCase(SebConstants.Accounts.AVAILABLE_BALANCE);
    }

    public Amount toAmount() {
        return Optional.ofNullable(balanceAmount)
                .map(b -> new Amount(b.getCurrency(), b.getAmount()))
                .orElse(BalancesEntity.Default);
    }
}
