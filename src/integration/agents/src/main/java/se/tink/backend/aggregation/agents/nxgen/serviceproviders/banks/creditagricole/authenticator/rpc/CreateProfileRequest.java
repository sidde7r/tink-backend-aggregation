package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreateProfileRequest {

    private String accountNumber;
    private String email;
    private String password;

    public CreateProfileRequest(String accountNumber, String email, String password) {
        this.accountNumber = accountNumber;
        this.email = email;
        this.password = password;
    }
}
