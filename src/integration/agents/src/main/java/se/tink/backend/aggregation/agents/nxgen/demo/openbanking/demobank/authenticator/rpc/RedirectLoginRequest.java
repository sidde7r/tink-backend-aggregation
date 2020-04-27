package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.rpc;

import se.tink.backend.aggregation.nxgen.http.form.Form;

public class RedirectLoginRequest {
    private String code;
    private String redirectUri;
    private String grantType;

    public RedirectLoginRequest(String code, String redirectUri) {
        this.code = code;
        this.redirectUri = redirectUri;
        this.grantType = "authorization_code";
    }

    public String toData() {
        return Form.builder()
                .put("code", code)
                .put("redirectUri", redirectUri)
                .put("grant_type", grantType)
                .build()
                .serialize();
    }
}
