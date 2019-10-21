package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole;

import com.google.common.base.Preconditions;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.CreditAgricoleConstants.ApiServices;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.CreditAgricoleConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.CreditAgricoleConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.CreditAgricoleConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.CreditAgricoleConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.CreditAgricoleConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.authenticator.AuthorizePointsEnum;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.configuration.CreditAgricoleConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity.HalPaymentRequestCreation;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity.HalPaymentRequestEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity.PaymentRequestResourceEntity;
import se.tink.backend.aggregation.configuration.EidasProxyConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public final class CreditAgricoleApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private CreditAgricoleConfiguration configuration;
    protected EidasProxyConfiguration eidasConf;

    public CreditAgricoleApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
    }

    private CreditAgricoleConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    protected void setConfiguration(CreditAgricoleConfiguration configuration, EidasProxyConfiguration eidasConf) {
        this.configuration = Preconditions.checkNotNull(configuration);
        this.eidasConf = Preconditions.checkNotNull(eidasConf);
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequestInSession(URL url) {
        final String authToken = "Bearer " + getTokenFromStorage();
        return createRequest(url).header(HeaderKeys.AUTHORIZATION, authToken);
    }

    private String getTokenFromStorage() {
        // TODO refactor error throw when oAuth2Token expired
        OAuth2Token oAuth2Token =
            persistentStorage
                .get(OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new RuntimeException("Unable to load oauth_2_token"));
        return oAuth2Token.getAccessToken();
    }

    public GetAccountsResponse getAccounts() {
        return createRequestInSession(getUrl(ApiServices.ACCOUNTS))
            .get(GetAccountsResponse.class);
    }

    public GetTransactionsResponse getTransactions(String id) {
        return createRequestInSession(
                getUrl(ApiServices.TRANSACTIONS).parameter(IdTags.ACCOUNT_ID, id))
                .get(GetTransactionsResponse.class);
    }

    public HalPaymentRequestCreation makePayment(
            PaymentRequestResourceEntity paymentRequestResourceEntity) {

        String body = SerializationUtils.serializeToString(paymentRequestResourceEntity);

        return createRequestInSession(getUrl(ApiServices.CREATE_PAYMENT_REQUEST))
                .type(MediaType.APPLICATION_JSON)
                .post(HalPaymentRequestCreation.class, body);
    }

    public HalPaymentRequestEntity fetchPayment(String uniqueId) {
        return createRequestInSession(
                        getUrl(ApiServices.FETCH_PAYMENT_REQUEST)
                        .parameter(IdTags.PAYMENT_REQUEST_RESOURCE_ID, uniqueId))
                .get(HalPaymentRequestEntity.class);
    }

    public TokenResponse getToken(String code) {
        final String clientId = getConfiguration().getClientId();
        final String redirectUri = getConfiguration().getRedirectUrl();

        TokenRequest request =
                new TokenRequest.TokenRequestBuilder()
                        .scope(QueryValues.SCOPE)
                        .grantType(QueryValues.GRANT_TYPE)
                        .code(code)
                        .redirectUri(redirectUri)
                        .clientId(clientId).build();

        return client.request(getTokenUrl())
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
            .accept(MediaType.APPLICATION_JSON)
            .header(HeaderKeys.CORRELATION_ID, UUID.randomUUID().toString())
            .header(HeaderKeys.CATS_CONSOMMATEUR, HeaderValues.CATS_CONSOMMATEUR)
            .header(HeaderKeys.CATS_CONSOMMATEURORIGINE, HeaderValues.CATS_CONSOMMATEURORIGINE)
            .header(HeaderKeys.CATS_CANAL, HeaderValues.CATS_CANAL)
            .post(TokenResponse.class, request.toData());
    }

    public TokenResponse refreshToken(String refreshToken) {
        final String clientId = getConfiguration().getClientId();
        final String redirectUri = getConfiguration().getRedirectUrl();

        TokenRequest request =
            new TokenRequest.TokenRequestBuilder()
                .scope(QueryValues.SCOPE)
                .grantType(QueryValues.REFRESH_TOKEN)
                .refreshToken(refreshToken)
                .redirectUri(redirectUri)
                .clientId(clientId).build();

        return client.request(getTokenUrl())
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
            .accept(MediaType.APPLICATION_JSON)
            .header(HeaderKeys.CORRELATION_ID, UUID.randomUUID().toString())
            .header(HeaderKeys.CATS_CONSOMMATEUR, HeaderValues.CATS_CONSOMMATEUR)
            .header(HeaderKeys.CATS_CONSOMMATEURORIGINE, HeaderValues.CATS_CONSOMMATEURORIGINE)
            .header(HeaderKeys.CATS_CANAL, HeaderValues.CATS_CANAL)
            .post(TokenResponse.class, request.toData());
    }

    private String getTokenUrl() {
        return getBaseUrl() + ApiServices.TOKEN;
    }

    public void setTokenToSession(OAuth2Token token) {
        persistentStorage.put(OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN, token);
    }

    private URL getUrl(String apiService) {
        URL url = new URL(getBaseUrl() + apiService);
        System.out.println(url.get());
        return url;
    }

    private String getBaseUrl() {
        //TODO refactor exception
        AuthorizePointsEnum bank = persistentStorage.get(CreditAgricoleConstants.StorageKeys.BANK_URL, AuthorizePointsEnum.class).orElseThrow(() -> new RuntimeException("Unable to load correct bank url"));
        return bank.getBaseUrl();
    }

}
