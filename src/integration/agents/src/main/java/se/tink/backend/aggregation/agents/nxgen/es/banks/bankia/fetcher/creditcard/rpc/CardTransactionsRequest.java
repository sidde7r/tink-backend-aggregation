package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.creditcard.entities.SearchCriteriaEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardTransactionsRequest {
    @JsonProperty("identificadorTarjeta")
    private String cardNumberUnmasked;

    @JsonProperty("criteriosBusquedaMovimientosTarjeta")
    private SearchCriteriaEntity searchCriteria;

    @JsonIgnore
    private CardTransactionsRequest(String cardNumberUnmasked, int limit) {
        this.cardNumberUnmasked = cardNumberUnmasked;
        searchCriteria = new SearchCriteriaEntity();
        searchCriteria.setLimit(limit);
    }

    @JsonIgnore
    public static CardTransactionsRequest create(String cardNumberUnmasked, int limit) {
        return new CardTransactionsRequest(cardNumberUnmasked, limit);
    }
}
