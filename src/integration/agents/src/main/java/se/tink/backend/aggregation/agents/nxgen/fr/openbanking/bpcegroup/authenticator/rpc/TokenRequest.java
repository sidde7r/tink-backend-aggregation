package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.authenticator.rpc;

import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;

public class TokenRequest extends AbstractForm {

    private TokenRequest(String redirectUri, String code, String clientId) {
        put("client_id", clientId);
        put("redirect_uri", redirectUri);
        put("grant_type", "authorization_code");
        put("code", code);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String redirectUri;
        private String code;
        private String clientId;

        public Builder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder redirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
            return this;
        }

        public TokenRequest build() {
            return new TokenRequest(this.redirectUri, this.code, this.clientId);
        }
    }
}
