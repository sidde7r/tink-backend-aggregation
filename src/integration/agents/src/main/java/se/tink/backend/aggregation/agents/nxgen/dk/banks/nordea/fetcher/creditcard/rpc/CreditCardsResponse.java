package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.creditcard.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.creditcard.entities.CreditCardEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditCardsResponse {
    private List<CreditCardEntity> cards;

    public List<CreditCardEntity> getCards() {
        return cards;
    }
}
