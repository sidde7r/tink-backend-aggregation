package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.List;

@JsonObject
public class CardsDataEntity {
    private List<CardSummaryEntity> cardSummaryList;

    public List<CardSummaryEntity> getCardSummaryList() {
        return cardSummaryList;
    }
}
