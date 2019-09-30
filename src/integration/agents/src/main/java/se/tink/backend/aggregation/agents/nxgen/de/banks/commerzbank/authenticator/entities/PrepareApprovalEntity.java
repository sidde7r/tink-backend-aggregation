package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PrepareApprovalEntity {
    private String approvalMethod;
    private String imageBase64;
    private String mobileNumber;
    private String serverChallenge;

    public String getApprovalMethod() {
        return approvalMethod;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getServerChallenge() {
        return serverChallenge;
    }
}
