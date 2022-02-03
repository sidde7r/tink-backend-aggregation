package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator.rpc;

import lombok.Builder;
import lombok.Setter;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.FormKeys;
import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;

@Builder
@Setter
public class TokenFormCredentials extends AbstractForm {
    private String clientId;
    private String grantType;
    private String clientSecret;

    private TokenFormCredentials(String clientId, String grantType, String clientSecret) {
        put(FormKeys.CLIENT_ID, clientId);
        put(FormKeys.GRANT_TYPE, grantType);
        put(FormKeys.CLIENT_SECRET, clientSecret);
    }

    public TokenFormCredentials build() {
        return new TokenFormCredentials(this.clientId, this.grantType, this.clientSecret);
    }
}
