package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankIdInitRequest {
    private String ssn;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String mobileNumber;

    public BankIdInitRequest(String ssn, String mobileNumber) {
        this.ssn = ssn;
        this.mobileNumber = mobileNumber;
    }

    public BankIdInitRequest(String ssn) {
        this.ssn = ssn;
    }

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }
}
