package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.authenticator.entity;

import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class ClientCredentialsTokenRequest extends AbstractForm {

    private ClientCredentialsTokenRequest(String clientId, String grantType, String scope) {
        put("client_id", clientId);
        put("grant_type", grantType);
        put("scope", scope);
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
