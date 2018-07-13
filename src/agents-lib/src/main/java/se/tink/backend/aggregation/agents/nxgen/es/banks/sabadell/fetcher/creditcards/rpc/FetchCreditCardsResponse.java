package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.creditcards.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.creditcards.entities.CreditCardEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.entities.PaginatorEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchCreditCardsResponse {
    private List<CreditCardEntity> cards;
    private PaginatorEntity paginator;

    public List<CreditCardEntity> getCards() {
        return cards;
    }

    public PaginatorEntity getPaginator() {
        return paginator;
    }
}
