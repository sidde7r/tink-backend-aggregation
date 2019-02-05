package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.creditcard.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.creditcard.entities.CardEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchCardResponse {
    private List<CardEntity> cardArray;

    public List<CardEntity> getCardArray() {
        return cardArray;
    }
}
