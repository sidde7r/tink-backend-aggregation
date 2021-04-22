package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.creditcard.entities.CardTransactionsResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FindMovementsResponse {

    @JsonProperty("FindMovementsResponse")
    private CardTransactionsResponse cardTransactionsResponse;

    public CardTransactionsResponse getCardTransactionsResponse() {
        return cardTransactionsResponse;
    }
}
