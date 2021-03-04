package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.loan.entities;

import java.math.BigDecimal;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Getter
public class AmountEntity {
    private BigDecimal granted;
    private BigDecimal drawn;
    private BigDecimal undrawn;
    private BigDecimal paid;
    private BigDecimal balance;

    public ExactCurrencyAmount getTinkBalance() {
        return new ExactCurrencyAmount(balance, NordeaBaseConstants.CURRENCY).negate();
    }

    public ExactCurrencyAmount getTinkAmortized() {
        return new ExactCurrencyAmount(paid, NordeaBaseConstants.CURRENCY);
    }

    public ExactCurrencyAmount getTinkInitialBalance() {
        return new ExactCurrencyAmount(granted, NordeaBaseConstants.CURRENCY);
    }
}
