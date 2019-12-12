package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PensionPlanEntity extends AbstractContractDetailsEntity {
    private AmountEntity liquidValue;

    private double shares;
    private AmountEntity balance;

    public AmountEntity getLiquidValue() {
        return liquidValue;
    }

    public double getShares() {
        return shares;
    }

    public AmountEntity getBalance() {
        return balance;
    }
}
