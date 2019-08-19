package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.BalanceTypes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class BalanceEntity {
    public static final ExactCurrencyAmount DEFAULT =
            ExactCurrencyAmount.of(BigDecimal.ZERO, "EUR");

    private AmountEntity balanceAmount;
    private String balanceType;
    private String lastChangeDateTime;
    private String referenceDate;

    @JsonIgnore
    public boolean isAvailableBalance() {
        return BalanceTypes.BALANCES.contains(balanceType.toLowerCase());
    }

    @JsonIgnore
    public ExactCurrencyAmount toAmount() {
        return balanceAmount.toAmount();
    }

    public AmountEntity getBalanceAmount() {
        return balanceAmount;
    }
}
