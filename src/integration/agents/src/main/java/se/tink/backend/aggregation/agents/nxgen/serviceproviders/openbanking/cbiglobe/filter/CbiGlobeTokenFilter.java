package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.filter;

import java.util.Optional;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiUrlProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.CbiGlobeConfiguration;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.TokenResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@RequiredArgsConstructor
public class CbiGlobeTokenFilter extends Filter {

    private final TinkHttpClient client;
    private final CbiStorage storage;
    private final CbiGlobeConfiguration configuration;
    private final CbiUrlProvider urlProvider;

    @Override
    public HttpResponse handle(HttpRequest httpRequest) {
        if (!httpRequest.getUrl().toString().contains(CbiUrlProvider.TOKEN)) {
            ensureValidTokenPresentInStorage();
            OAuth2Token token =
                    storage.getToken()
                            .orElseThrow(
                                    () ->
                                            new IllegalStateException(
                                                    "Token not in storage when expected!"));
            httpRequest.getHeaders().add(HttpHeaders.AUTHORIZATION, token.toAuthorizeHeader());
        }
        return nextFilter(httpRequest);
    }

    private void ensureValidTokenPresentInStorage() {
        Optional<OAuth2Token> maybeToken = storage.getToken();
        if (!maybeToken.isPresent() || !maybeToken.get().isValid()) {
            TokenResponse tokenResponse = acquireNewToken();
            storage.saveToken(tokenResponse.toTinkToken());
        }
    }

    private TokenResponse acquireNewToken() {
        return client.request(urlProvider.getTokenUrl())
                .type(MediaType.APPLICATION_JSON)
                .addBasicAuth(configuration.getClientId(), configuration.getClientSecret())
                .queryParam(QueryKeys.GRANT_TYPE, QueryValues.CLIENT_CREDENTIALS)
                .queryParam(QueryKeys.SCOPE, QueryValues.PRODUCTION)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenResponse.class, null);
    }
}
