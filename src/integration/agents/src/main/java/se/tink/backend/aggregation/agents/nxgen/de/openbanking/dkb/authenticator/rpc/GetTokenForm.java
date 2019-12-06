package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbConstants.FormKeys;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class GetTokenForm extends AbstractForm {
    private GetTokenForm(String grantType, String username, String password) {
        put(FormKeys.GRANT_TYPE, grantType);
        put(FormKeys.USERNAME, username);
        put(FormKeys.PASSWORD, password);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String grantType;
        private String username;
        private String password;

        public Builder setGrantType(String grantType) {
            this.grantType = grantType;
            return this;
        }

        public Builder setUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder setPassword(String password) {
            this.password = password;
            return this;
        }

        public GetTokenForm build() {
            return new GetTokenForm(this.grantType, this.username, this.password);
        }
    }
}
