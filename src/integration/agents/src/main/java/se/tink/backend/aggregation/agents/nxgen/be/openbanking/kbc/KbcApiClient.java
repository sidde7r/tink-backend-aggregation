package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc;

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.KbcConstants.OAuth;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.KbcConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.KbcConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.entities.TransactionResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity.AccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.ConsentBaseRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.ConsentBaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.RefreshTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.TokenBaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.TokenRequestPost;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.configuration.BerlinGroupConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.executor.payment.enums.BerlinGroupPaymentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.executor.payment.rpc.GetPaymentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc.BerlinGroupAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc.TransactionsKeyPaginatorBaseResponse;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

public final class KbcApiClient extends BerlinGroupApiClient<BerlinGroupConfiguration> {

    private final Credentials credentials;
    private final PersistentStorage persistentStorage;

    public KbcApiClient(
            final TinkHttpClient client,
            final SessionStorage sessionStorage,
            final Credentials credentials,
            final PersistentStorage persistentStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.credentials = credentials;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public BerlinGroupAccountResponse fetchAccounts() {
        return getAccountsRequestBuilder(getConfiguration().getBaseUrl() + Urls.ACCOUNTS)
                .header(Psd2Headers.Keys.CONSENT_ID, persistentStorage.get(StorageKeys.CONSENT_ID))
                .header(Psd2Headers.Keys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .header(Psd2Headers.Keys.PSU_IP_ADDRESS, getConfiguration().getPsuIpAddress())
                .get(AccountResponse.class);
    }

    @Override
    public TransactionsKeyPaginatorBaseResponse fetchTransactions(String url) {
        return null;
    }

    public TransactionResponseEntity fetchTransactions(
            TransactionalAccount account, Date dateFrom, Date dateTo) {
        URL url =
                new URL(getConfiguration().getBaseUrl().concat(Urls.TRANSACTIONS))
                        .parameter(KbcConstants.IdTags.ACCOUNT_ID, account.getApiIdentifier());

        return getTransactionsRequestBuilder(url.toString())
                .header(BerlinGroupConstants.HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                .queryParam(BerlinGroupConstants.QueryKeys.BOOKING_STATUS, QueryValues.BOOKED)
                .queryParam(
                        KbcConstants.QueryKeys.DATE_FROM,
                        ThreadSafeDateFormat.FORMATTER_DAILY.format(dateFrom))
                .queryParam(
                        KbcConstants.QueryKeys.DATE_TO,
                        ThreadSafeDateFormat.FORMATTER_DAILY.format(dateTo))
                .get(TransactionResponseEntity.class);
    }

    @Override
    public RequestBuilder getTransactionsRequestBuilder(final String url) {
        return client.request(url)
                .addBearerToken(getTokenFromSession(StorageKeys.OAUTH_TOKEN))
                .header(
                        BerlinGroupConstants.HeaderKeys.PSU_IP_ADDRESS,
                        getConfiguration().getPsuIpAddress())
                .header(
                        BerlinGroupConstants.HeaderKeys.CONSENT_ID,
                        persistentStorage.get(StorageKeys.CONSENT_ID));
    }

    public URL getAuthorizeUrl(final String state) {
        final String consentId = getConsentId();
        persistentStorage.put(StorageKeys.CONSENT_ID, consentId);
        final String authUrl = Urls.BASE_AUTH_URL + Urls.AUTH;
        return getAuthorizeUrlWithCode(
                        authUrl,
                        state,
                        consentId,
                        getConfiguration().getClientId(),
                        getConfiguration().getRedirectUrl())
                .getUrl();
    }

    @Override
    public OAuth2Token getToken(final String code) {
        final TokenRequestPost tokenRequest =
                new TokenRequestPost(
                        FormValues.AUTHORIZATION_CODE,
                        code,
                        getConfiguration().getRedirectUrl(),
                        getConfiguration().getClientId(),
                        getConfiguration().getClientSecret(),
                        sessionStorage.get(StorageKeys.CODE_VERIFIER));

        return client.request(getConfiguration().getBaseUrl() + Urls.TOKEN)
                .addBasicAuth(
                        getConfiguration().getClientId(), getConfiguration().getClientSecret())
                .body(tokenRequest.toData(), MediaType.APPLICATION_FORM_URLENCODED)
                .header(
                        BerlinGroupConstants.HeaderKeys.PSU_IP_ADDRESS,
                        getConfiguration().getPsuIpAddress())
                .post(TokenBaseResponse.class)
                .toTinkToken();
    }

    @Override
    public OAuth2Token refreshToken(final String token) {
        final RefreshTokenRequest refreshTokenRequest =
                new RefreshTokenRequest(
                        FormValues.REFRESH_TOKEN_GRANT_TYPE,
                        token,
                        getConfiguration().getClientId(),
                        getConfiguration().getClientSecret());

        return client.request(getConfiguration().getBaseUrl() + Urls.TOKEN)
                .addBasicAuth(
                        getConfiguration().getClientId(), getConfiguration().getClientSecret())
                .header(
                        BerlinGroupConstants.HeaderKeys.PSU_IP_ADDRESS,
                        getConfiguration().getPsuIpAddress())
                .body(refreshTokenRequest.toData(), MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenBaseResponse.class)
                .toTinkToken();
    }

    @Override
    public String getConsentId() {
        final AccessEntity accessEntity =
                new AccessEntity.Builder()
                        .withBalances(
                                Arrays.asList(
                                        credentials.getField(KbcConstants.CredentialKeys.IBAN)))
                        .withTransactions(
                                Arrays.asList(
                                        credentials.getField(KbcConstants.CredentialKeys.IBAN)))
                        .build();

        final ConsentBaseRequest consentsRequest = new ConsentBaseRequest();
        consentsRequest.setAccess(accessEntity);

        return client.request(getConfiguration().getBaseUrl() + Urls.CONSENT)
                .body(consentsRequest.toData(), MediaType.APPLICATION_JSON_TYPE)
                .header(
                        BerlinGroupConstants.HeaderKeys.AUTHORIZATION,
                        KbcConstants.HeaderKeys.BEARER.concat(getConfiguration().getClientId()))
                .header(BerlinGroupConstants.HeaderKeys.X_REQUEST_ID, UUID.randomUUID())
                .header(
                        BerlinGroupConstants.HeaderKeys.TPP_REDIRECT_URI,
                        getConfiguration().getRedirectUrl())
                .header(
                        BerlinGroupConstants.HeaderKeys.PSU_IP_ADDRESS,
                        getConfiguration().getPsuIpAddress())
                .post(ConsentBaseResponse.class)
                .getConsentId();
    }

    public CreatePaymentResponse createPayment(
            CreatePaymentRequest createPaymentRequest,
            BerlinGroupPaymentType paymentType,
            String state) {
        return getPaymentRequestBuilder(
                        new URL(getConfiguration().getBaseUrl() + Urls.PAYMENTS)
                                .parameter(IdTags.PAYMENT_PRODUCT, paymentType.toString()))
                .header(
                        HeaderKeys.TPP_REDIRECT_URI,
                        getConfiguration().getRedirectUrl().concat("?state=").concat(state))
                .post(CreatePaymentResponse.class, createPaymentRequest);
    }

    public GetPaymentStatusResponse getPaymentStatus(
            String paymentId, BerlinGroupPaymentType paymentType) {
        return getPaymentRequestBuilder(
                        new URL(getConfiguration().getBaseUrl() + Urls.PAYMENT_STATUS)
                                .parameter(IdTags.PAYMENT_PRODUCT, paymentType.toString())
                                .parameter(IdTags.PAYMENT_ID, paymentId))
                .get(GetPaymentStatusResponse.class);
    }

    @Override
    protected RequestBuilder getPaymentRequestBuilder(final URL url) {
        return client.request(url)
                .addBearerToken(tokenFromClientId())
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(HeaderKeys.PSU_IP_ADDRESS, getConfiguration().getPsuIpAddress());
    }

    private OAuth2Token tokenFromClientId() {
        return OAuth2Token.create(OAuth.BEARER, getConfiguration().getClientId(), null, 864000);
    }
}
