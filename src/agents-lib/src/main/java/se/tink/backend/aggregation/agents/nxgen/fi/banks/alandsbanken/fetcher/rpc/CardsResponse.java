package se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.entities.AlandsBankenCard;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.rpc.AlandsBankenResponse;

public class CardsResponse extends AlandsBankenResponse {

    private List<AlandsBankenCard> cards;

    public List<AlandsBankenCard> getCards() {
        return cards;
    }

    public void setCards(List<AlandsBankenCard> cards) {
        this.cards = cards;
    }
}
