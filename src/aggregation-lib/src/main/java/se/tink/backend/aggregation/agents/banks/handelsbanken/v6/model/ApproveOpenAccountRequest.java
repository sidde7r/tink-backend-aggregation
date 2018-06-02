package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import java.util.List;

public class ApproveOpenAccountRequest {
    private List<AgreementEntity> agreements;
    private boolean agreed;

    public boolean isAgreed() {
        return agreed;
    }

    public void setAgreed(boolean agreed) {
        this.agreed = agreed;
    }

    public List<AgreementEntity> getAgreements() {
        return agreements;
    }

    public void setAgreements(List<AgreementEntity> agreements) {
        this.agreements = agreements;
    }
}
