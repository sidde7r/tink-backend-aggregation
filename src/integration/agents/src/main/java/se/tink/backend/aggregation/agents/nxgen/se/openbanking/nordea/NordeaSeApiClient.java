package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea;

import javax.ws.rs.HttpMethod;
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
import se.tink.libraries.serialization.utils.SerializationUtils;

public final class NordeaSeApiClient extends NordeaBaseApiClient {

    public NordeaSeApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        super(client, persistentStorage);

        this.client.addFilter(new NordeaSeFilter());
        this.client.addFilter(new TimeoutFilter());
    }

    public AuthorizeResponse authorize(AuthorizeRequest authorizeRequest) {
        String body = SerializationUtils.serializeToString(authorizeRequest);
        return createRequest(NordeaSeConstants.Urls.AUTHORIZE, HttpMethod.POST, body)
                .post(AuthorizeResponse.class, authorizeRequest);
    }

    public HttpResponse getCode(String orderRef, String token) {
        return createRequestWithTppToken(
                        new URL(NordeaSeConstants.Urls.GET_CODE + orderRef),
                        token,
                        HttpMethod.GET,
                        null)
                .get(HttpResponse.class);
    }

    public OAuth2Token getToken(GetTokenForm form, String token) {
        return createRequestWithTppToken(
                        NordeaSeConstants.Urls.GET_TOKEN,
                        token,
                        HttpMethod.POST,
                        form.getBodyValue())
                .body(form, MediaType.APPLICATION_FORM_URLENCODED)
                .post(GetTokenResponse.class)
                .toTinkToken();
    }
    /* The body needs to be in the right alphabetical order or else it will break
     * the underlying code has an LinkedHashMap which should fix this
     */
    @Override
    public OAuth2Token refreshToken(String refreshToken) {
        final String body = RefreshTokenForm.of(refreshToken).getBodyValue();
        return createRequest(NordeaSeConstants.Urls.GET_TOKEN, HttpMethod.POST, body)
                .body(body, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(GetTokenResponse.class)
                .toTinkToken();
    }

    private RequestBuilder createRequestWithTppToken(
            URL url, String token, String httpMethod, String body) {
        return createRequest(url, httpMethod, body)
                .header(
                        NordeaBaseConstants.HeaderKeys.AUTHORIZATION,
                        NordeaSeConstants.HeaderValues.TOKEN_TYPE + " " + token);
    }
}
