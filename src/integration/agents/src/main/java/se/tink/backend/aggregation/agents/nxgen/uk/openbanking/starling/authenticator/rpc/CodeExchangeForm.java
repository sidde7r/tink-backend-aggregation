package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.StarlingConstants;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class CodeExchangeForm extends AbstractForm {

    private CodeExchangeForm(String code, String clientId, String clientSecret, String grantType, String redirectUri) {
        put(StarlingConstants.RequestKey.CODE, code);
        put(StarlingConstants.RequestKey.CLIENT_ID, clientId);
        put(StarlingConstants.RequestKey.CLIENT_SECRET, clientSecret);
        put(StarlingConstants.RequestKey.GRANT_TYPE, grantType);
        put(StarlingConstants.RequestKey.REDIRECT_URI, redirectUri);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private static final String AUTHORIZATION_CODE = "authorization_code";

        private String code;
        private String clientId;
        private String clientSecret;
        private String redirectUri;

        public Builder withCode(String code) {
            this.code = code;
            return this;
        }

        public Builder asClient(String clientId, String clientSecret) {
            this.clientId = clientId;
            this.clientSecret = clientSecret;
            return this;
        }

        public Builder withRedirect(String redirectUri) {
            this.redirectUri = redirectUri;
            return this;
        }

        public CodeExchangeForm build() {
            return new CodeExchangeForm(code, clientId, clientSecret, AUTHORIZATION_CODE, redirectUri);
        }
    }
}
