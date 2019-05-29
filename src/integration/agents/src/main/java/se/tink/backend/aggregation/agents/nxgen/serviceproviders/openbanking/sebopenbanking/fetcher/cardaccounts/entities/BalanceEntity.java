package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.fetcher.cardaccounts.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.SebConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.fetcher.transactionalaccount.entities.BalancesEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

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
    public boolean isAvailableBalance() {
        return SebConstants.Accounts.AVAILABLE_BALANCE.equalsIgnoreCase(balanceType);
    }

    @JsonIgnore
    public Amount toAmount() {
        return Optional.ofNullable(balanceAmount)
                .map(b -> new Amount(b.getCurrency(), b.getAmount()))
                .orElse(BalancesEntity.getDefault());
    }
}
