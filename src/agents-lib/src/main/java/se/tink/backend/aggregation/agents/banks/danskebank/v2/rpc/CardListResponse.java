package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardListResponse extends BankIdOutput {
    @JsonProperty("OthersCards")
    private List<CardEntity> otherCards;
    @JsonProperty("Cards")
    private List<CardEntity>  cards;

    public List<CardEntity> getOtherCards() {
        return otherCards;
    }

    public void setOtherCards(List<CardEntity> otherCards) {
        this.otherCards = otherCards;
    }

    public List<CardEntity> getCards() {
        return cards;
    }

    public List<String> getCreditCardAccounts() {
        if (cards == null) {
            return Collections.emptyList();
        }

        ImmutableList<CardEntity> creditCards = FluentIterable.from(cards).filter(cardEntity -> cardEntity != null &&
                cardEntity.getCardName() != null &&
                !cardEntity.getCardName().isEmpty() &&
                !cardEntity.getCardName().toLowerCase().contains("bankkort")).toList();

        List<String> creditCardAccounts = Lists.newArrayList();
        for (CardEntity card : creditCards) {
            creditCardAccounts.add(card.getAccountNumber());
        }

        return creditCardAccounts;
    }

    public void setCards(List<CardEntity> cards) {
        this.cards = cards;
    }
}
