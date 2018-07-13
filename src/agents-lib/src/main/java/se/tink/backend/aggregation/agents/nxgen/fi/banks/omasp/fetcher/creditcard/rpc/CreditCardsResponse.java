package se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.creditcard.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.creditcard.entities.CreditCardEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.rpc.OmaspBaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditCardsResponse extends OmaspBaseResponse {
    private List<CreditCardEntity> cards;

    public List<CreditCardEntity> getCards() {
        return cards;
    }
}
