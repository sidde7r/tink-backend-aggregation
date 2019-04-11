package se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.SpareBank1Constants;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class GetTokenForm extends AbstractForm {
    private GetTokenForm(String grantType, String clientId, String clientSecret) {
        put(SpareBank1Constants.FormKeys.GRANT_TYPE, grantType);
        put(SpareBank1Constants.FormKeys.CLIENT_ID, clientId);
        put(SpareBank1Constants.FormKeys.CLIENT_SECRET, clientSecret);
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

        public GetTokenForm build() {
            return new GetTokenForm(this.grantType, this.clientId, this.clientSecret);
        }
    }
}
