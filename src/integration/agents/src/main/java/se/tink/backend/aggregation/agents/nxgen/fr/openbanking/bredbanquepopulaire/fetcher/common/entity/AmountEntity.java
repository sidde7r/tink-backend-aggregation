package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.common.entity;

import com.google.common.base.Preconditions;
import java.math.BigDecimal;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.transacitons.entity.CreditDebitIndicator;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Data
public class AmountEntity {
    private String currency;
    private BigDecimal amount;

    public ExactCurrencyAmount toTinkAmount() {
        Preconditions.checkNotNull(amount);
        Preconditions.checkNotNull(currency);

        return new ExactCurrencyAmount(amount, currency);
    }

    public ExactCurrencyAmount toTinkTransactionsAmount(CreditDebitIndicator creditDebitIndicator) {
        Preconditions.checkNotNull(amount);
        Preconditions.checkNotNull(currency);

        return CreditDebitIndicator.DBIT.equals(creditDebitIndicator)
                ? new ExactCurrencyAmount(amount.negate(), currency)
                : new ExactCurrencyAmount(amount, currency);
    }
}
