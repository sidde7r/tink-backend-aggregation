package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.investment.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TotalEquitiesEntity {
    private AmountEntity value;
    private AmountEntity acquisitionValue;
    private AmountEntity buyingPower;
    private AmountEntity totalValueInclCash;
    private AmountEntity changeAbsolute;
    private AmountEntity marketValue;
    private AmountEntity sumSettlements;
    private String changePercent;

    public AmountEntity getValue() {
        return value;
    }

    public AmountEntity getAcquisitionValue() {
        return acquisitionValue;
    }

    public AmountEntity getBuyingPower() {
        return buyingPower;
    }

    public AmountEntity getTotalValueInclCash() {
        return totalValueInclCash;
    }

    public AmountEntity getChangeAbsolute() {
        return changeAbsolute;
    }

    public AmountEntity getMarketValue() {
        return marketValue;
    }

    public AmountEntity getSumSettlements() {
        return sumSettlements;
    }

    public String getChangePercent() {
        return changePercent;
    }
}
