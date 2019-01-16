package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2;

import javax.ws.rs.core.MediaType;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.authenticator.rpc.InitAuthRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.authenticator.rpc.InitAuthResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.authenticator.rpc.OauthTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.authenticator.rpc.PollAuthResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.Form;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class NordeaBaseApiClient {

    private final TinkHttpClient client;
    private final NordeaSessionStorage sessionStorage;
    private final NordeaPersistentStorage persistentStorage;

    public NordeaBaseApiClient(TinkHttpClient client, NordeaSessionStorage sessionStorage,
            NordeaPersistentStorage persistentStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.persistentStorage = persistentStorage;
    }

    public InitAuthResponse initAuthorization(InitAuthRequest request) {
        return createAuthRequest(NordeaBaseConstants.Url.AUTHORIZE_DECOUPLED)
                .post(InitAuthResponse.class, request);
    }

    public PollAuthResponse pollAuthCode(String collectPath, String tppTokenHeaderValue) {

        HttpResponse response = createAuthRequest(NordeaBaseConstants.Url.getUrlForLink(collectPath))
                .header(HttpHeaders.AUTHORIZATION, tppTokenHeaderValue)
                .get(HttpResponse.class);

        if (response.getStatus() == HttpStatus.SC_OK) {
            return response.getBody(PollAuthResponse.class);
        }

        throw new HttpResponseException(response.getRequest(), response);
    }

    public TokenResponse getAccessToken(String tokenPath, String tppTokenHeaderValue, TokenRequest request) {
        return createAuthRequest(NordeaBaseConstants.Url.getUrlForLink(tokenPath))
                .header(HttpHeaders.AUTHORIZATION, tppTokenHeaderValue)
                .post(TokenResponse.class, request);
    }

    public OauthTokenResponse oauthExchangeCodeForAccessToken(String code) {
        Form request = Form.builder()
                .put(NordeaBaseConstants.Query.CODE, code)
                .put(NordeaBaseConstants.Query.REDIRECT_URI, persistentStorage.getRedirectUrl())
                .build();

        return createAuthRequest(NordeaBaseConstants.Url.ACCESS_TOKEN, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(OauthTokenResponse.class, request.serialize());
    }

    public TokenResponse exchangeCodeForAccessToken(String code) {
        Form request = Form.builder()
                .put(NordeaBaseConstants.Query.CODE, code)
                .put(NordeaBaseConstants.Query.REDIRECT_URI, persistentStorage.getRedirectUrl())
                .build();

        return createAuthRequest(NordeaBaseConstants.Url.ACCESS_TOKEN, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(TokenResponse.class, request.serialize());
    }

    public AccountsResponse fetchAccounts() {
        return createApiRequest(NordeaBaseConstants.Url.ACCOUNTS)
                .get(AccountsResponse.class);
    }

    public TransactionsResponse fetchTransactions(String path) {
        return createApiRequest(NordeaBaseConstants.Url.getUrlForLink(path))
                .get(TransactionsResponse.class);
    }

    private RequestBuilder createAuthRequest(URL url, MediaType contentType) {
        return client.request(url)
                .header(NordeaBaseConstants.Header.CLIENT_ID, persistentStorage.getClientId())
                .header(NordeaBaseConstants.Header.CLIENT_SECRET, persistentStorage.getClientsecret())
                .type(contentType);
    }

    private RequestBuilder createAuthRequest(URL url) {
        return createAuthRequest(url, MediaType.APPLICATION_JSON_TYPE);
    }

    private RequestBuilder createApiRequest(URL url) {
        OAuth2Token accessToken = sessionStorage.getAccessToken()
                .orElseThrow(() -> new IllegalStateException("No access token found"));
        return createAuthRequest(url)
                .header(HttpHeaders.AUTHORIZATION, accessToken.toAuthorizeHeader());
    }
}
