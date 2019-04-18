package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank;

import com.fasterxml.jackson.databind.ObjectMapper;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.rpc.AuthorizationRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.rpc.AuthorizationResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.rpc.ExchangeTokenForm;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.rpc.ExchangeTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.rpc.TokenForm;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.configuration.OpBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

public final class OpBankApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private OpBankConfiguration configuration;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public OpBankApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
    }

    private OpBankConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    protected void setConfiguration(OpBankConfiguration configuration) {
        this.configuration = configuration;
    }

    public TokenResponse fetchNewToken() {

        HttpResponse response =
                client.request(Urls.OAUTH_TOKEN)
                        .accept(MediaType.APPLICATION_OCTET_STREAM_TYPE)
                        .body(
                                new TokenForm()
                                        .setClientId(this.configuration.getClientId())
                                        .setClientSecret(this.configuration.getClientSecret()),
                                MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                        .post(HttpResponse.class);

        try {
            return MAPPER.readValue(response.getBodyInputStream(), TokenResponse.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public AuthorizationResponse createNewAuthorization(String bearerToken) {
        HttpResponse response = client.request(Urls.ACCOUNTS_AUTHORIZATION)
            .type(MediaType.APPLICATION_JSON_TYPE)
            .accept(MediaType.APPLICATION_JSON_TYPE)
            .body(AuthorizationRequest.expiresInDays(60))
            .header(HeaderKeys.X_API_KEY, this.configuration.getApiKey())
            .header(HeaderKeys.X_FAPI_FINANCIAL_ID, "test")
            .header(HeaderKeys.AUTHORIZATION, "Bearer " + bearerToken)
            .post(HttpResponse.class);

        try {
            return MAPPER.readValue(response.getBodyInputStream(), AuthorizationResponse.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public String fetchSignature(String jwt) {
        //return client.request("https://localhost:8080/sign")
        return client.request("https://localhost:8080/sign")
            .header("Token", jwt)
            .get(String.class);
    }

    public ExchangeTokenResponse exchangeToken(String code) {
        HttpResponse response = client.request(Urls.OAUTH_TOKEN)
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .accept(MediaType.APPLICATION_JSON)
                .body(
                        new ExchangeTokenForm()
                                .setClientId(this.configuration.getClientId())
                                .setClientSecret(this.configuration.getClientSecret())
                                .setCode(code)
                                .setRedirectUri(this.configuration.getRedirectUrl())
                ).post(HttpResponse.class);
        try {
            return MAPPER.readValue(response.getBodyInputStream(), ExchangeTokenResponse.class);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public GetAccountsResponse getAccounts() {
        HttpResponse response = client.request(Urls.GET_ACCOUNTS)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .header(HeaderKeys.X_API_KEY, this.configuration.getApiKey())
                .header(HeaderKeys.X_FAPI_FINANCIAL_ID, "test")
                .header(HeaderKeys.X_CUSTOMER_USER_AGENT, "tink")
                .header(HeaderKeys.X_FAPI_CUSTOMER_IP_ADDRESS, "127.0.0.1")
                .addBearerToken(persistentStorage
                        .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class).get())
                .get(HttpResponse.class);

        try {
            return MAPPER.readValue(response.getBodyInputStream(), GetAccountsResponse.class);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public GetTransactionsResponse getTransactions(String accountId) {

        HttpResponse response = createRequest(new URL(Urls.GET_TRANSACTIONS).parameter("accountId", accountId))
                .header(HeaderKeys.X_API_KEY, this.configuration.getApiKey())
                .header(HeaderKeys.X_FAPI_CUSTOMER_IP_ADDRESS, "127.0.0.1")
                .header(HeaderKeys.X_CUSTOMER_USER_AGENT, "tink")
                .header(HeaderKeys.X_FAPI_INTERACTION_ID, UUID.randomUUID().toString())
                .addBearerToken(persistentStorage
                        .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class).get())
                .get(HttpResponse.class);
        try {
            return MAPPER.readValue(response.getBodyInputStream(), GetTransactionsResponse.class);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        // TODO last login time?
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequestInSession(URL url) {
        final OAuth2Token authToken = getTokenFromStorage();

        return createRequest(url).addBearerToken(authToken);
    }

    private OAuth2Token getTokenFromStorage() {
        return persistentStorage
                .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception()));
    }
}
