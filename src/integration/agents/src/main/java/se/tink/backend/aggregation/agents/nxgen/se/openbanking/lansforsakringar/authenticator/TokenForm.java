package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class TokenForm extends AbstractForm {

    private TokenForm(
            String grantType,
            String clientId,
            String clientSecret) {
        put(LansforsakringarConstants.FormKeys.GRANT_TYPE, grantType);
        put(LansforsakringarConstants.FormKeys.CLIENT_ID, clientId);
        put(LansforsakringarConstants.FormKeys.CLIENT_SECRET, clientSecret);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String grantType;
        private String clientId;
        private String clientSecret;

        public Builder setGrantType(String grantType) {
            this.grantType = grantType;
            return this;
        }

        public Builder setClientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
            return this;
        }

        public TokenForm build() {
            return new TokenForm(
                    this.grantType, this.clientId, this.clientSecret);
        }
    }
}
