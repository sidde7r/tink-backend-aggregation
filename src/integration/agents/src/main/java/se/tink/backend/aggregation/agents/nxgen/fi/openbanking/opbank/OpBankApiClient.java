package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.rpc.AuthorizationRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.rpc.AuthorizationResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.rpc.ExchangeTokenForm;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.rpc.ExchangeTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.rpc.RefreshTokenForm;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.rpc.TokenForm;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.configuration.OpBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcAlg;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.QsealcSignerImpl;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class OpBankApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private OpBankConfiguration configuration;
    private String redirectUrl;
    private EidasProxyConfiguration eidasProxyConfiguration;
    private EidasIdentity eidasIdentity;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public OpBankApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
    }

    protected void setConfiguration(
            AgentConfiguration<OpBankConfiguration> agentConfiguration,
            EidasProxyConfiguration eidasProxyConfiguration,
            EidasIdentity eidasIdentity) {
        this.configuration = agentConfiguration.getProviderSpecificConfiguration();
        this.redirectUrl = agentConfiguration.getRedirectUrl();
        this.eidasProxyConfiguration = eidasProxyConfiguration;
        client.setEidasProxy(eidasProxyConfiguration);
        this.eidasIdentity = eidasIdentity;
    }

    public TokenResponse fetchNewToken() {
        HttpResponse response;

        try {
            response =
                    client.request(Urls.OAUTH_TOKEN)
                            .accept(MediaType.APPLICATION_OCTET_STREAM_TYPE)
                            .body(
                                    new TokenForm()
                                            .setClientId(configuration.getClientId())
                                            .setClientSecret(configuration.getClientSecret()),
                                    MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                            .post(HttpResponse.class);
        } catch (HttpResponseException e) {
            throw mapServiceException(e);
        }

        try {
            return MAPPER.readValue(response.getBodyInputStream(), TokenResponse.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public OAuth2Token fetchRefreshToken(String refreshToken) {
        final String clientId = configuration.getClientId();
        final String clientSecret = configuration.getClientSecret();
        RefreshTokenForm refreshTokenForm =
                RefreshTokenForm.builder()
                        .setClientId(clientId)
                        .setClientSecret(clientSecret)
                        .setGrantType(OpBankConstants.RefreshTokenFormKeys.REFRESH_TOKEN)
                        .setRefreshToken(refreshToken)
                        .build();
        return client.request(Urls.OAUTH_TOKEN)
                .accept(MediaType.APPLICATION_OCTET_STREAM_TYPE)
                .body(refreshTokenForm, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(TokenResponse.class)
                .toTinkToken();
    }

    public AuthorizationResponse createNewAuthorization(String bearerToken) {
        HttpResponse response =
                client.request(Urls.ACCOUNTS_AUTHORIZATION)
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .body(AuthorizationRequest.expiresInDays(90))
                        .header(HeaderKeys.X_API_KEY, configuration.getApiKey())
                        .header(HeaderKeys.X_FAPI_FINANCIAL_ID, HeaderValues.TINK)
                        .header(HeaderKeys.AUTHORIZATION, "Bearer " + bearerToken)
                        .post(HttpResponse.class);

        try {
            return MAPPER.readValue(response.getBodyInputStream(), AuthorizationResponse.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String fetchSignature(String jwt) {
        QsealcSigner signer =
                QsealcSignerImpl.build(
                        eidasProxyConfiguration.toInternalConfig(),
                        QsealcAlg.EIDAS_RSA_SHA256,
                        eidasIdentity);
        byte[] signatureBytes = signer.getSignature(jwt.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().encodeToString(signatureBytes);
    }

    public ExchangeTokenResponse exchangeToken(String code) {
        HttpResponse response =
                client.request(Urls.OAUTH_TOKEN)
                        .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                        .accept(MediaType.APPLICATION_JSON)
                        .body(
                                new ExchangeTokenForm()
                                        .setClientId(configuration.getClientId())
                                        .setClientSecret(configuration.getClientSecret())
                                        .setCode(code)
                                        .setRedirectUri(redirectUrl))
                        .post(HttpResponse.class);
        try {
            return MAPPER.readValue(response.getBodyInputStream(), ExchangeTokenResponse.class);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public GetAccountsResponse getAccounts() {
        HttpResponse response =
                client.request(Urls.GET_ACCOUNTS)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .header(HeaderKeys.X_API_KEY, configuration.getApiKey())
                        .header(HeaderKeys.X_FAPI_FINANCIAL_ID, HeaderValues.TINK)
                        .header(HeaderKeys.X_CUSTOMER_USER_AGENT, HeaderValues.TINK)
                        .header(
                                HeaderKeys.X_FAPI_CUSTOMER_IP_ADDRESS,
                                HeaderValues.CUSTOMER_IP_ADRESS)
                        .addBearerToken(
                                persistentStorage
                                        .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                                        .get())
                        .get(HttpResponse.class);

        try {
            return MAPPER.readValue(response.getBodyInputStream(), GetAccountsResponse.class);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public GetTransactionsResponse getTransactions(URL url) {

        HttpResponse response =
                createRequest(url)
                        .header(HeaderKeys.X_API_KEY, this.configuration.getApiKey())
                        .header(
                                HeaderKeys.X_FAPI_CUSTOMER_IP_ADDRESS,
                                HeaderValues.CUSTOMER_IP_ADRESS)
                        .header(HeaderKeys.X_CUSTOMER_USER_AGENT, HeaderValues.TINK)
                        .header(HeaderKeys.X_FAPI_INTERACTION_ID, UUID.randomUUID().toString())
                        .addBearerToken(
                                persistentStorage
                                        .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                                        .get())
                        .get(HttpResponse.class);
        try {
            return MAPPER.readValue(response.getBodyInputStream(), GetTransactionsResponse.class);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private BankServiceException mapServiceException(HttpResponseException exception) {
        if (exception.getResponse().getStatus() == 503) {
            return BankServiceError.BANK_SIDE_FAILURE.exception();
        } else {
            throw exception;
        }
    }
}
