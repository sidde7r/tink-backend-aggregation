package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OpBankWinLoseEntity {
    private OpBankAmountEntity winLoseAmount;
    private double winLosePct;

    public OpBankAmountEntity getWinLoseAmount() {
        return winLoseAmount;
    }

    public double getWinLosePct() {
        return winLosePct;
    }
}
