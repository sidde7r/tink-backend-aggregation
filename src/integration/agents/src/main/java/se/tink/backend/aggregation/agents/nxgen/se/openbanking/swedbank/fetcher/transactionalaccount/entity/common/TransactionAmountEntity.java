package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.entity.common;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class TransactionAmountEntity {
    private String currency;
    private String amount;

    public String getCurrency() {
        return currency;
    }

    public String getContent() {
        return amount;
    }

    public Amount toTinkAmount() {
        return new Amount(currency, StringUtils.parseAmount(amount));
    }
}
