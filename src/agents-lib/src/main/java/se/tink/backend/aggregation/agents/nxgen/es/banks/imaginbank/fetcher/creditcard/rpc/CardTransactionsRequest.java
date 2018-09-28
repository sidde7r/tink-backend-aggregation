package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.creditcard.entities.FilterEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardTransactionsRequest {

    @JsonProperty("filtroLista")
    private FilterEntity filter;
    private String numItemsPag;
    @JsonProperty("inicio")
    private boolean start;

    public CardTransactionsRequest(String cardId, boolean start) {
        numItemsPag = ImaginBankConstants.CreditCard.NUM_ITEMS_PAGE;
        filter = new FilterEntity(cardId);
        this.start =  start;
    }
}
