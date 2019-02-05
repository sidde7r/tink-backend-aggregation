package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.useractivation;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TargetUrlRequest {
    private String agreementId;

    public String getAgreementId() {
        return agreementId;
    }

    public void setAgreementId(String agreementId) {
        this.agreementId = agreementId;
    }
}
