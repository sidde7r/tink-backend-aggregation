package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OpBankHoldingsEntity {
    private double ownedPcs;
    private double sellablePcs;
    private OpBankPriceEurEntity marketPrice;
    private OpBankPriceEurEntity marketValue;
    private String quotationCurrency;

    public double getOwnedPcs() {
        return ownedPcs;
    }

    public OpBankPriceEurEntity getMarketPrice() {
        return marketPrice;
    }

    public OpBankPriceEurEntity getMarketValue() {
        return marketValue;
    }
}
