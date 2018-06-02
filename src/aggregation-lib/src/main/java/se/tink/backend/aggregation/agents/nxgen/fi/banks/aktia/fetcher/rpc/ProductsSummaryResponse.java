package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ProductsSummaryResponse {
    private MessagesSummaryEntity messagesSummary;
    private CardsSummary cardsSummary;
    private InvestmentsSummary investmentsSummary;

    public MessagesSummaryEntity getMessagesSummary() {
        return messagesSummary;
    }

    public CardsSummary getCardsSummary() {
        return cardsSummary;
    }

    public InvestmentsSummary getInvestmentsSummary() {
        return investmentsSummary;
    }
}
