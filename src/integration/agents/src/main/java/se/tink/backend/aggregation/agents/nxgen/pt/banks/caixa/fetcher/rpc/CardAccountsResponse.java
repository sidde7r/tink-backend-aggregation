package se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.entities.CardAccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardAccountsResponse {

    private List<CardAccountEntity> cardAccounts;

    public List<CardAccountEntity> getCardAccounts() {
        return cardAccounts;
    }
}
