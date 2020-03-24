package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.authenticator.rpc;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.AbancaConstants.FormKeys;
import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;

public class TokenRequest extends AbstractForm {

    private TokenRequest(Builder builder) {
        put(FormKeys.GRANT_TYPE, builder.grantType);
        put(FormKeys.APPLICATION, builder.clientId);
        Optional.ofNullable(builder.code).ifPresent(c -> put(FormKeys.CODE, c));
        Optional.ofNullable(builder.refreshToken).ifPresent(rt -> put(FormKeys.REFRESH_TOKEN, rt));
    }

    public static Builder builder(String clientId, String grantType) {
        return new Builder(clientId, grantType);
    }

    public static class Builder {
        private String clientId;
        private String grantType;
        private String code;
        private String refreshToken;

        private Builder(String clientId, String grantType) {
            this.clientId = clientId;
            this.grantType = grantType;
        }

        public Builder setCode(String code) {
            this.code = code;
            return this;
        }

        public Builder setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public TokenRequest build() {
            return new TokenRequest(this);
        }
    }
}
