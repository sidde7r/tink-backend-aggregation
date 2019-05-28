package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.SebConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class BalancesEntity {

    private String balanceType;
    private String creditLimitIncluded;
    private BalanceAmountEntity balanceAmount;

    public boolean isAvailableBalance() {
        return balanceType.equalsIgnoreCase(SebConstants.Accounts.AVAILABLE_BALANCE);
    }

    public Amount toAmount() {
        return Optional.ofNullable(balanceAmount)
                .map(b -> new Amount(b.getCurrency(), b.getAmount()))
                .orElse(BalancesEntity.getDefault());
    }

    @JsonIgnore
    public static Amount getDefault() {
        return Amount.inSEK(0);
    }
}
