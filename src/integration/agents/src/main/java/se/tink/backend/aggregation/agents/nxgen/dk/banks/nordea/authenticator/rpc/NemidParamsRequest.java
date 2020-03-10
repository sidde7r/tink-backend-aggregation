package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class NemidParamsRequest {

    private String acrValues = null;
    private String clientId = "CDi170IiCEmvEbxWn3Hk";
    private String codeChallenge;
    private String codeChallengeMethod = "S256";
    private String nonce;
    private String redirectUri = "com.nordea.MobileBankDK://auth-callback";
    private String responseType = "code";
    private String scope = "openid ndf agreement offline_access";
    private String signingOrderId = null;
    private String state;

    public static Builder builder() {
        return new Builder();
    }

    private NemidParamsRequest(String codeChallenge, String nonce, String state) {
        this.codeChallenge = codeChallenge;
        this.nonce = nonce;
        this.state = state;
    }

    public static class Builder {
        private String codeChallenge;
        private String nonce;
        private String state;

        public Builder withCodeChallenge(String codeChallenge) {
            this.codeChallenge = codeChallenge;
            return this;
        }

        public Builder withNonce(String nonce) {
            this.nonce = nonce;
            return this;
        }

        public Builder withState(String state) {
            this.state = state;
            return this;
        }

        public NemidParamsRequest build() {
            return new NemidParamsRequest(codeChallenge, nonce, state);
        }
    }
}
