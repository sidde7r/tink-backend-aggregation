package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.entity.balance;

import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.BalanceType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Getter
public class BalancesItem {
    private String balanceType;
    private BalanceAmount balanceAmount;
    private String referenceDate;

    public ExactCurrencyAmount toTinkAmount() {
        return balanceAmount.getAmount();
    }

    public boolean isBooked() {
        return balanceType.equalsIgnoreCase(BalanceType.BOOKED)
                || balanceType.equalsIgnoreCase(BalanceType.AUTHORISED);
    }

    public boolean isAvailable() {
        return balanceType.equalsIgnoreCase(BalanceType.AVAILABLE);
    }
}
