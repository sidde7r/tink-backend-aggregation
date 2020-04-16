package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.fetcher.transactionalaccount.entities;

import static se.tink.libraries.amount.ExactCurrencyAmount.of;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbConstants.BalanceTypes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class BalanceEntity {

    @JsonIgnore public static final ExactCurrencyAmount Default = of(BigDecimal.ZERO, "EUR");

    private AmountEntity balanceAmount;
    private String balanceType;

    @JsonIgnore
    public boolean isAvailable() {
        return balanceType.equalsIgnoreCase(BalanceTypes.INTERIM_AVAILABLE)
                || balanceType.equalsIgnoreCase(BalanceTypes.FORWARD_AVAILABLE)
                || balanceType.equalsIgnoreCase(BalanceTypes.CLOSING_BOOKED);
    }

    @JsonIgnore
    public ExactCurrencyAmount getAmount() {
        return balanceAmount.toAmount();
    }
}
