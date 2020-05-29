package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PrimaryAuthRequest extends DefaultAuthRequest {

    private String accountCode;
    private String userId;
    private String grantType = "password";
    private String accountNumber;

    public PrimaryAuthRequest(String accountCode, String userId, String accountNumber) {
        super("primary_auth");
        this.accountCode = accountCode;
        this.userId = userId;
        this.accountNumber = accountNumber;
    }
}
