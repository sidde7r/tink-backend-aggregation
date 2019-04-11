package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.entities.FilterEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardTransactionsRequest {

    @JsonProperty("filtroLista")
    private FilterEntity filter;

    private String numItemsPag = "100";

    @JsonProperty("inicio")
    private boolean start;

    public CardTransactionsRequest(String cardId, boolean start) {
        filter = new FilterEntity(cardId);
        this.start = start;
    }
}
