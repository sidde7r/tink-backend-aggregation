package se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.entities.CardAccountTransactionsEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.entities.CardEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardAccountTransactionsResponse {

    private CardAccountTransactionsEntity cardAccountTransactions;
    private List<CardEntity> cards;

    public CardAccountTransactionsEntity getCardAccountTransactions() {
        return cardAccountTransactions;
    }

    public List<CardEntity> getCards() {
        return cards;
    }
}
