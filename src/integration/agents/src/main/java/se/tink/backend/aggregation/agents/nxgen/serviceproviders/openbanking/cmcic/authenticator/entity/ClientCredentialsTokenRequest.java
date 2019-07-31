package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.authenticator.entity;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.FormKeys;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class ClientCredentialsTokenRequest extends AbstractForm {

    private ClientCredentialsTokenRequest(String clientId, String grantType, String scope) {
        put(FormKeys.CLIENT_ID, clientId);
        put(FormKeys.GRANT_TYPE, grantType);
        put(FormKeys.SCOPE, scope);
    }

    public static ClientCredentialsTokenRequestBuilder builder() {
        return new ClientCredentialsTokenRequestBuilder();
    }

    public static class ClientCredentialsTokenRequestBuilder {

        private String clientId;
        private String grantType;
        private String scope;

        ClientCredentialsTokenRequestBuilder() {}

        public ClientCredentialsTokenRequestBuilder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public ClientCredentialsTokenRequestBuilder grantType(String grantType) {
            this.grantType = grantType;
            return this;
        }

        public ClientCredentialsTokenRequestBuilder scope(String scope) {
            this.scope = scope;
            return this;
        }

        public ClientCredentialsTokenRequest build() {
            return new ClientCredentialsTokenRequest(clientId, grantType, scope);
        }
    }
}
