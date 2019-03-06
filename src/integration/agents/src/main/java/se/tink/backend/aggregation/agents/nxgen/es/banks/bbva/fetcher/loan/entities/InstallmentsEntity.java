package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.loan.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InstallmentsEntity {
    private String installmentDate;
    private AmountEntity installmentAmount;

    public String getInstallmentDate() {
        return installmentDate;
    }

    public AmountEntity getInstallmentAmount() {
        return installmentAmount;
    }
}
