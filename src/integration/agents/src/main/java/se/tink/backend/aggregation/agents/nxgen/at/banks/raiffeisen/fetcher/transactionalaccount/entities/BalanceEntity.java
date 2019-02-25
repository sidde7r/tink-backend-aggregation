package se.tink.backend.aggregation.agents.nxgen.at.banks.raiffeisen.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class BalanceEntity extends Amount {
    public BalanceEntity(@JsonProperty("Currency") String currency, @JsonProperty("Amount") String value) {
        super(currency, StringUtils.parseAmount(value));
    }
}
