package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.rpc;

public class InitBankIdRequest {
    private String ssn;

    public String getSsn() {
        return ssn;
    }

    public InitBankIdRequest setSsn(String ssn) {
        this.ssn = ssn;
        return this;
    }
}
