package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.creditcard.entities.CardDetail;

public class CardDetailResponse {

    @JsonProperty("CardDetailResponse")
    private CardDetail cardDetail;

    public CardDetail getCardDetail() {
        return cardDetail;
    }
}
