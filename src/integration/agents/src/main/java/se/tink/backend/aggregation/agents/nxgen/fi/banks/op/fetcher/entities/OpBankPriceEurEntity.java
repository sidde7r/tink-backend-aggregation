package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OpBankPriceEurEntity {
    private OpBankAmountEntity priceEur;
    private String marketTimeForShares;
    private OpBankAmountEntity priceQuotationCurrency;

    public OpBankAmountEntity getPriceEur() {
        return priceEur;
    }

    public String getMarketTimeForShares() {
        return marketTimeForShares;
    }

    public OpBankAmountEntity getPriceQuotationCurrency() {
        return priceQuotationCurrency;
    }
}
