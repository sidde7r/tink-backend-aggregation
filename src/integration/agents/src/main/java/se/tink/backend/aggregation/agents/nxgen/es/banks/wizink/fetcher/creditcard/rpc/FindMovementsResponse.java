package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.creditcard.entities.CreditCardTransactions;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FindMovementsResponse {

    @JsonProperty("FindMovementsResponse")
    private CreditCardTransactions creditCardTransactions;

    public CreditCardTransactions getCreditCardTransactions() {
        return creditCardTransactions;
    }
}
