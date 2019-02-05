package se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.fetchers.transactional.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AmountEntity {
    private String currency;
    private String value;

    public String getCurrency() {
        return currency;
    }

    public String getValue() {
        return value;
    }

    @JsonIgnore
    public Amount getAsTinkAmount() {
        return new Amount(currency, AgentParsingUtils.parseAmount(value));
    }
}
