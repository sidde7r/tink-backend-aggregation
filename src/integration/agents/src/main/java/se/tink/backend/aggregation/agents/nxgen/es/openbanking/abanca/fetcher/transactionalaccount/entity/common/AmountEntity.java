package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.fetcher.transactionalaccount.entity.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class AmountEntity {

    @JsonProperty("cantidad")
    private String value;

    @JsonProperty("divisa")
    private String currency;

    public Amount toTinkAmount() {
        return new Amount(currency, StringUtils.parseAmount(value));
    }
}
