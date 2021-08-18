package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

public class BankIdLoginRequest {
    private String reference;
    private String ssn;

    public BankIdLoginRequest(String reference, String ssn) {
        this.reference = reference;
        this.ssn = ssn;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }
}
