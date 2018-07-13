package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import com.google.common.collect.Lists;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardListResponse {
    private List<CardEntity> cards;
    private String emptyDisclaimer;

    public List<CardEntity> getCards() {
        return cards;
    }

    public void setCards(List<CardEntity> cards) {
        this.cards = cards;
    }

    public String getEmptyDisclaimer() {
        return emptyDisclaimer;
    }

    public void setEmptyDisclaimer(String emptyDisclaimer) {
        this.emptyDisclaimer = emptyDisclaimer;
    }

    public List<CardEntity> toCardEntityList() {
        if (cards == null) {
            return Lists.newArrayList();
        }
        return cards;
    }
}
