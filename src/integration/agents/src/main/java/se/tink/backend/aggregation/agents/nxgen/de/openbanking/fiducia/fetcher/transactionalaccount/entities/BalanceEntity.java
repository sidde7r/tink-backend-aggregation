package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.fetcher.transactionalaccount.entities;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BalanceEntity {

    private static final String AVAILABLE = "available";
    private static final String CLOSING_BOOKED = "closingBooked";

    private AmountEntity balanceAmount;
    private String balanceType;

    public boolean isAvailableBalance() {
        return balanceType.equalsIgnoreCase(AVAILABLE)
                || balanceType.equalsIgnoreCase(CLOSING_BOOKED);
    }

    public ExactCurrencyAmount getAmount() {
        return balanceAmount.toAmount();
    }
}
