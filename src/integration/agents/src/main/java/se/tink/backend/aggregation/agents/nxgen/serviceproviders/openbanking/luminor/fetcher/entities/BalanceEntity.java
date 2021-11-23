package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.entities;

import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.LuminorConstants.BalanceType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Getter
public class BalanceEntity {
    private BalanceAmountEntity balanceAmount;
    private String balanceType;
    private boolean creditLimitIncluded;

    public ExactCurrencyAmount toTinkAmount() {
        if (balanceAmount.getAmount() == null) {
            throw new IllegalStateException("Balance amount is not available");
        }
        return ExactCurrencyAmount.of(
                Double.parseDouble(balanceAmount.getAmount()), balanceAmount.getCurrency());
    }

    protected boolean isBooked() {
        return balanceType.equalsIgnoreCase(BalanceType.BOOKED);
    }

    protected boolean isAvailable() {
        return balanceType.equalsIgnoreCase(BalanceType.AVAILABLE);
    }
}
