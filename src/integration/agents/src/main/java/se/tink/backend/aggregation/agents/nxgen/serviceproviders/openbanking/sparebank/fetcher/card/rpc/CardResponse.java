package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.card.rpc;

import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.card.entities.CardEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardResponse {

    private List<CardEntity> cardAccounts;

    public List<CardEntity> getCardAccounts() {
        return cardAccounts == null ? Collections.emptyList() : cardAccounts;
    }
}
