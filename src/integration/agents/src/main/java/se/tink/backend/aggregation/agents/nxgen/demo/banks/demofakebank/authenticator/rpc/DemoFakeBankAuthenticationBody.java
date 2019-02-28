package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DemoFakeBankAuthenticationBody {
    public String username;
    public String password;

    public DemoFakeBankAuthenticationBody(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
