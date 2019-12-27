package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.detail;

import static java.util.Objects.requireNonNull;

import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class AuthorizationURLBuilder {
    private final String clientId;
    private final String redirectUrl;
    private final String state;

    public AuthorizationURLBuilder(String clientId, String redirectUrl, String state) {
        this.clientId = requireNonNull(clientId);
        this.redirectUrl = requireNonNull(redirectUrl);
        this.state = requireNonNull(state);
    }

    public URL buildAuthorizationURL() {
        return ChebancaConstants.Urls.AUTHORIZE
                .queryParam(
                        ChebancaConstants.QueryKeys.RESPONSE_TYPE,
                        ChebancaConstants.QueryValues.CODE)
                .queryParam(ChebancaConstants.QueryKeys.CLIENT_ID, clientId)
                .queryParam(ChebancaConstants.QueryKeys.REDIRECT_URI, redirectUrl)
                .queryParam(ChebancaConstants.QueryKeys.STATE, state);
    }
}
