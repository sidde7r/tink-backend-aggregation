package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea;

import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.rpc.AuthorizeRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.rpc.AuthorizeResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.rpc.GetTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.filter.NordeaSeFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.rpc.GetTokenForm;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.rpc.RefreshTokenForm;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TimeoutFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class NordeaSeApiClient extends NordeaBaseApiClient {

    public NordeaSeApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        super(client, persistentStorage);

        this.client.addFilter(new NordeaSeFilter());
        this.client.addFilter(new TimeoutFilter());
    }

    private RequestBuilder createRequestWithTppToken(URL url, String token) {
        return createRequest(url)
                .header(
                        NordeaBaseConstants.HeaderKeys.AUTHORIZATION,
                        NordeaSeConstants.HeaderValues.TOKEN_TYPE + " " + token);
    }

    public AuthorizeResponse authorize(AuthorizeRequest authorizeRequest) {
        return createRequest(NordeaSeConstants.Urls.AUTHORIZE)
                .post(AuthorizeResponse.class, authorizeRequest);
    }

    public HttpResponse getCode(String orderRef, String token) {
        return createRequestWithTppToken(new URL(NordeaSeConstants.Urls.GET_CODE + orderRef), token)
                .get(HttpResponse.class);
    }

    public OAuth2Token getToken(GetTokenForm form, String token) {
        return createRequestWithTppToken(NordeaSeConstants.Urls.GET_TOKEN, token)
                .body(form, MediaType.APPLICATION_FORM_URLENCODED)
                .post(GetTokenResponse.class)
                .toTinkToken();
    }

    @Override
    public OAuth2Token refreshToken(String refreshToken) {
        return createRequest(NordeaSeConstants.Urls.GET_TOKEN)
                .body(RefreshTokenForm.of(refreshToken), MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(GetTokenResponse.class)
                .toTinkToken();
    }
}
