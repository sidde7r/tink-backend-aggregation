package se.tink.backend.aggregation.agents.creditcards.ikano.api.responses.engagements;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import java.util.List;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.IkanoApiAgent;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.responses.BaseResponse;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardEntities extends BaseResponse {
    private List<CardEntity> cards;

    public List<Account> getTinkAccounts() {
        List<Account> tinkAccounts = Lists.newArrayList();

        for (CardEntity card : cards) {
            tinkAccounts.add(card.toTinkAccount());
        }

        return tinkAccounts;
    }

    public List<CardEntity> getCards() {
        return cards != null ? cards : Lists.<CardEntity>newArrayList();
    }

    public CardEntity getCardFor(Account account)
            throws IkanoApiAgent.AccountRelationNotFoundException {
        for (CardEntity card : getCards()) {
            if (card.isRelatedTo(account)) {
                return card;
            }
        }

        throw new IkanoApiAgent.AccountRelationNotFoundException();
    }

    private void ensureHasCards() throws LoginException {
        if (cards == null || cards.isEmpty()) {
            throw LoginError.NOT_CUSTOMER.exception();
        }
    }

    public void keepSelectedCardTypes(CardType cardType) throws LoginException {
        ensureHasCards();

        List<CardEntity> cardsWithCorrectType = Lists.newArrayList();

        for (CardEntity card : cards) {
            if (card.isOfType(cardType)) {
                cardsWithCorrectType.add(card);
            }
        }

        cards = cardsWithCorrectType;
        ensureHasCards();
    }

    @JsonProperty("Response")
    public void setCards(List<CardEntity> cards) {
        this.cards = cards;
    }
}
