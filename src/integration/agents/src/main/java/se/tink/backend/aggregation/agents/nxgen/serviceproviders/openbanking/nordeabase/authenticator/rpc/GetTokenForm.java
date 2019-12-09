package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class GetTokenForm extends AbstractForm {
    private GetTokenForm(String grantType, String code, String redirectUri) {
        put(NordeaBaseConstants.FormKeys.GRANT_TYPE, grantType);
        put(NordeaBaseConstants.FormKeys.CODE, code);
        put(NordeaBaseConstants.FormKeys.REDIRECT_URI, redirectUri);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String grantType;
        private String code;
        private String redirectUri;

        public Builder setGrantType(String grantType) {
            this.grantType = grantType;
            return this;
        }

        public Builder setCode(String code) {
            this.code = code;
            return this;
        }

        public Builder setRedirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
            return this;
        }

        public GetTokenForm build() {
            return new GetTokenForm(this.grantType, this.code, this.redirectUri);
        }
    }
}
