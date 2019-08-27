package se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.fetcher.transactionalaccount.entity.balance;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class BalanceAmountEntity {

    private String currency;
    private String amount;

    public String getCurrency() {
        return currency;
    }

    public Amount toAmount() {
        return new Amount(currency, StringUtils.parseAmount(amount));
    }
}
