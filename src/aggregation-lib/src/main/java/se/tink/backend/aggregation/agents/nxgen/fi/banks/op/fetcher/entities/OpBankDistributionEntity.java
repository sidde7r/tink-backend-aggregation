package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OpBankDistributionEntity {
    private OpBankAmountEntity acquisitionValue;
    private OpBankAmountEntity marketValue;
    private double distributionPct;
    private OpBankAmountEntity winLoseEur;
    private double winLosePct;

    public OpBankAmountEntity getAcquisitionValue() {
        return acquisitionValue;
    }

    public OpBankAmountEntity getMarketValue() {
        return marketValue;
    }

    public double getDistributionPct() {
        return distributionPct;
    }

    public OpBankAmountEntity getWinLoseEur() {
        return winLoseEur;
    }

    public double getWinLosePct() {
        return winLosePct;
    }
}
