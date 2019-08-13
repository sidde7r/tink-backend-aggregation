package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.cards.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.cards.entity.CardAccountsItem;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardAccountsResponse {

    private List<CardAccountsItem> cardAccounts;

    public List<CardAccountsItem> getCardAccounts() {
        return cardAccounts;
    }
}
