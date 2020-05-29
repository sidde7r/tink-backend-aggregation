package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreateUserRequest extends DefaultAuthRequest {

    private String accountCode;
    private String grantType = "password";
    private String accountNumber;

    public CreateUserRequest(String accountCode, String accountNumber) {
        super("create_user");
        this.accountCode = accountCode;
        this.accountNumber = accountNumber;
    }
}
