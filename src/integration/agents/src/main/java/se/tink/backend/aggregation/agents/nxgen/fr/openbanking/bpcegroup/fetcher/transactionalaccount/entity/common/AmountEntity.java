package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.fetcher.transactionalaccount.entity.common;

import com.google.common.base.Preconditions;
import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AmountEntity {

    private String amount;
    private String currency;

    public ExactCurrencyAmount toTinkAmount() {
        Preconditions.checkNotNull(amount);
        Preconditions.checkNotNull(currency);

        return new ExactCurrencyAmount(new BigDecimal(amount), currency);
    }
}
