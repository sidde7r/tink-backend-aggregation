package se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class TransactionAmountEntity {
    @JsonProperty
    private String amount;

    @JsonProperty
    private String currency;

    public Amount getAmount() {
        return new Amount(currency, StringUtils.parseAmount(amount));
    }
}
