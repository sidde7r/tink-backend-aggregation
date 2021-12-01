package se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.fetcher.transactional.entities;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.LhvConstants.BalanceType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class BalanceEntity {
    private String balanceType;
    private BalanceAmountEntity balanceAmount;

    public ExactCurrencyAmount toTinkAmount() {
        return Optional.ofNullable(balanceAmount)
                .map(balance -> new ExactCurrencyAmount(balance.getAmount(), balance.getCurrency()))
                .orElseThrow(() -> new IllegalStateException("Could not parse amount"));
    }

    protected boolean isAvailable() {
        return BalanceType.AVAILABLE.equalsIgnoreCase(balanceType);
    }

    protected boolean isBooked() {
        return BalanceType.BOOKED.equalsIgnoreCase(balanceType);
    }
}
