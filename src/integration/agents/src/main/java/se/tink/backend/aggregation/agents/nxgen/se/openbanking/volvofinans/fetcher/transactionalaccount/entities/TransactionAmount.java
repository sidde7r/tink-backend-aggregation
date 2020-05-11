package se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans.fetcher.transactionalaccount.entities;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class TransactionAmount {

    private String currency;
    private String amount;

    public String getCurrency() {
        return currency;
    }

    public String getAmount() {
        return amount;
    }

    public ExactCurrencyAmount toAmount() {
        return ExactCurrencyAmount.of(
                StringUtils.parseAmount(Preconditions.checkNotNull(Strings.emptyToNull(amount))),
                Preconditions.checkNotNull(Strings.emptyToNull(currency)));
    }
}
