package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.nxgen.http.Form;

public class AuthenticationCodeRequest {
    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("response_type")
    private String responseTypel;

    private String scope;
    private String psuId;
    private String password;
    private String authenticationMethodId;
    private String scaAuthenticationData;

    public AuthenticationCodeRequest(
            String clientId,
            String responseTypel,
            String scope,
            String psuId,
            String password,
            String authenticationMethodId,
            String scaAuthenticationData) {
        this.clientId = clientId;
        this.responseTypel = responseTypel;
        this.scope = scope;
        this.psuId = psuId;
        this.password = password;
        this.authenticationMethodId = authenticationMethodId;
        this.scaAuthenticationData = scaAuthenticationData;
    }

    public String toData(String consentId) {
        return Form.builder()
                .put("client_id", "my-secret-client")
                .put("response_type", "code")
                .put("scope", "AIS: " + consentId)
                .put("psuId", "chipTAN")
                .put("password", "okok1")
                .put("authenticationMethodId", "QR")
                .put("scaAuthenticationData", "111111")
                .build()
                .serialize();
    }
}
