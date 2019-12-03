package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginInstalledRequest {

    private String userId;
    private String pinCode;
    private String installId;

    public LoginInstalledRequest(String userId, String pinCode, String installId) {
        this.userId = userId;
        this.pinCode = pinCode;
        this.installId = installId;
    }
}
