package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator.rpc;

import lombok.Builder;
import lombok.Setter;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.FormKeys;
import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;

@Builder
@Setter
public class TokenFormOauth extends AbstractForm {
    private String clientId;
    private String grantType;
    private String code;
    private String clientSecret;
    private String redirectUri;

    private TokenFormOauth(
            String clientId,
            String grantType,
            String code,
            String clientSecret,
            String redirectUri) {

        put(FormKeys.CLIENT_ID, clientId);
        put(FormKeys.GRANT_TYPE, grantType);
        put(FormKeys.CLIENT_SECRET, clientSecret);
        put(FormKeys.CODE, code);
        put(FormKeys.REDIRECT_URI, redirectUri);
    }

    public TokenFormOauth build() {
        return new TokenFormOauth(
                this.clientId, this.grantType, this.code, this.clientSecret, this.redirectUri);
    }
}
