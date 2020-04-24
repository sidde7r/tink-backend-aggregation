package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.form.Form;

@JsonObject
public class LoginReqest {
    private String username;
    private String password;
    private String grant_type;

    public LoginReqest(String username, String password, String grant_type) {
        this.username = username;
        this.password = password;
        this.grant_type = grant_type;
    }

    public String toData() {
        return Form.builder()
                .put("username", username)
                .put("password", password)
                .put("grant_type", grant_type)
                .build()
                .serialize();
    }
}
