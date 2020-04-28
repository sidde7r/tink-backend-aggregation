package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.rpc;

import se.tink.backend.aggregation.nxgen.http.form.Form;

public class RedirectRefreshTokenRequest {
    private String refreshtoken;
    private String grantType;

    public RedirectRefreshTokenRequest(String refreshtoken) {
        this.refreshtoken = refreshtoken;
        this.grantType = "refresh_token";
    }

    public String toData() {
        return Form.builder()
                .put("refresh_token", refreshtoken)
                .put("grant_type", grantType)
                .build()
                .serialize();
    }
}
