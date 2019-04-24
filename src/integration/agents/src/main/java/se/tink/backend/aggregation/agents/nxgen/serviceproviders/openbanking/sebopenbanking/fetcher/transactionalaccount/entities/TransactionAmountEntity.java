package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class TransactionAmountEntity {
    private String amount;

    private String currency;

    public Amount getAmount() {
        return new Amount(currency, StringUtils.parseAmount(amount));
    }
}
