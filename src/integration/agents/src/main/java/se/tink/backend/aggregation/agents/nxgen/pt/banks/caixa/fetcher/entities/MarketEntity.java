package se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MarketEntity {
    private String quoteMarketCode;
    private String quoteMarketDescription;

    public String getQuoteMarketCode() {
        return quoteMarketCode;
    }

    public String getQuoteMarketDescription() {
        return quoteMarketDescription;
    }
}
