package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardsSummary {
    private String summaryResultCode;
    private String summaryResult;
    private CardsDataEntity cardsData;

    public String getSummaryResultCode() {
        return summaryResultCode;
    }

    public String getSummaryResult() {
        return summaryResult;
    }

    public CardsDataEntity getCardsData() {
        return cardsData;
    }
}
