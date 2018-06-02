package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc;

public class SelectAgreementRequest {
    private String userNumber;
    private String agreementNumber;

    public String getUserNumber() {
        return userNumber;
    }

    public SelectAgreementRequest setUserNumber(String userNumber) {
        this.userNumber = userNumber;
        return this;
    }

    public String getAgreementNumber() {
        return agreementNumber;
    }

    public SelectAgreementRequest setAgreementNumber(String agreementNumber) {
        this.agreementNumber = agreementNumber;
        return this;
    }
}
