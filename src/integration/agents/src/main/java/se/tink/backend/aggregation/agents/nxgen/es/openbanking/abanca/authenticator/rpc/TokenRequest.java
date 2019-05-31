package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.AbancaConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.Form;

@JsonObject
public class TokenRequest {

    private String application;
    private String grantType;
    private String username;
    private String password;
    private String apiKey;

    public TokenRequest(
            String application, String grantType, String username, String password, String apiKey) {
        this.application = application;
        this.grantType = grantType;
        this.username = username;
        this.password = password;
        this.apiKey = apiKey;
    }

    public String toData() {
        return Form.builder()
                .put(AbancaConstants.QueryKeys.APPLICATION, application)
                .put(AbancaConstants.QueryKeys.GRANT_TYPE, grantType)
                .put(AbancaConstants.QueryKeys.USERNAME, username)
                .put(AbancaConstants.QueryKeys.PASSWORD, password)
                .put(AbancaConstants.QueryKeys.API_KEY, apiKey)
                .build()
                .serialize();
    }
}
