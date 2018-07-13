package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.creditcard.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.creditcard.entities.CardsEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.creditcard.entities.CreditCardEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardsOverviewResponse {
    private List<CardsEntity> cards;
    private List<CreditCardEntity> credits;

    public List<CardsEntity> getCards() {
        return cards;
    }

    public List<CreditCardEntity> getCredits() {
        return credits;
    }
}
