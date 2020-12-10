package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.card.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.card.entities.CardTransactionsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.entities.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.entities.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardTransactionResponse {

    private CardTransactionsEntity cardTransactions;

    public CardTransactionsEntity getCardTransactions() {
        return cardTransactions == null ? new CardTransactionsEntity() : cardTransactions;
    }

    @JsonIgnore
    public Optional<String> getNext() {
        return Optional.ofNullable(cardTransactions)
                .map(CardTransactionsEntity::getLinks)
                .map(LinksEntity::getNext)
                .map(LinkEntity::getHref);
    }
}
