package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.form.Form;

@JsonObject
public class PasswordLoginRequest {
    private String username;
    private String password;
    private String grantType;

    public PasswordLoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
        this.grantType = "password";
    }

    public String toData() {
        return Form.builder()
                .put("username", username)
                .put("password", password)
                .put("grant_type", grantType)
                .build()
                .serialize();
    }
}
