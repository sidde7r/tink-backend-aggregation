package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.FidorConstants.QueryKeys;
import se.tink.backend.aggregation.nxgen.http.Form;

public class TokenRequest {
    public String grantType;
    public String username;
    public String password;

    public TokenRequest(String grantType, String username, String password) {
        this.grantType = grantType;
        this.username = username;
        this.password = password;
    }

    public String toOautPasswordData() {
        return Form.builder()
                .put(QueryKeys.GRANT_TYPE, grantType)
                .put(QueryKeys.USERNAME, username)
                .put(QueryKeys.PASSWORD, password)
                .build()
                .serialize();
    }
}
