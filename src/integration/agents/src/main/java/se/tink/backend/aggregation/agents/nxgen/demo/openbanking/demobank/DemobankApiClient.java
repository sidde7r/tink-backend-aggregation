package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank;

import java.util.Optional;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.entities.TokenEntity;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.rpc.LoginReqest;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.configuration.DemobankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.fetcher.transactionalaccount.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class DemobankApiClient {

    private static final String REDIRECT_HOST = "https://cdn.tink.se/fake-bank/redirect-v3.html";
    private DemobankConfiguration configuration;
    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;

    public DemobankApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    public DemobankConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException("MISSING_CONFIGURATION"));
    }

    public URL getAuthorizeUrl(String state) {
        return new URL(REDIRECT_HOST);
    }

    public OAuth2Token getToken(String code) {
        return null;
    }

    public void setTokenToSession(OAuth2Token accessToken) {}

    public OAuth2Token refreshToken(String refreshToken) {
        return null;
    }

    public void setConfiguration(DemobankConfiguration clientConfiguration) {
        this.configuration = clientConfiguration;
    }

    public FetchAccountResponse fetchAccounts() {
        final URL url = fetchBaseUrl().concat(Urls.ACCOUNTS);
        OAuth2Token token =
                OAuth2Token.createBearer(
                        sessionStorage.get("accessToken"),
                        sessionStorage.get("refreshToken"),
                        Long.parseLong(sessionStorage.get("expiresIn")));
        return createRequestInSession(url, token).get(FetchAccountResponse.class);
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE);
    }

    private RequestBuilder createRequestInSession(URL url, OAuth2Token token) {
        return createRequest(url).addBearerToken(token);
    }

    public URL fetchBaseUrl() {
        return new URL("http://localhost:3001");
    }

    public TokenEntity login(String username, String password) {

        return createRequest(fetchBaseUrl().concat(Urls.OAUTH_TOKEN))
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .addBasicAuth("client", "password")
                .post(TokenEntity.class, new LoginReqest(username, password, "password").toData());
    }

    public FetchTransactionsResponse fetchTransactions(String accountId) {
        final URL url = fetchBaseUrl().concat(Urls.TRANSACTIONS).parameter("accountId", accountId);

        OAuth2Token token =
                OAuth2Token.createBearer(
                        sessionStorage.get("accessToken"),
                        sessionStorage.get("refreshToken"),
                        Long.parseLong(sessionStorage.get("expiresIn")));
        return createRequestInSession(url, token).get(FetchTransactionsResponse.class);
    }
}
