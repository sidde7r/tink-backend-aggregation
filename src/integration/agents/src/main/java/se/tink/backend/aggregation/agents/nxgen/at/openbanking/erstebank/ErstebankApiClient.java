package se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.ErstebankConstants.EndPoints;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.ErstebankConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.ErstebankConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.authenticator.rpc.AuthorisationConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.authenticator.rpc.AuthorisationConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.authenticator.rpc.ErsteAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.authenticator.rpc.ErsteKeyPaginatorResponse;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.authenticator.rpc.GetConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.authenticator.rpc.RefreshTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.configuration.ErstebankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.executor.payment.rpc.FetchPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity.AccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.ConsentBaseRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.ConsentBaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.TokenBaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.TokenRequestPost;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc.TransactionsKeyPaginatorBaseResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class ErstebankApiClient extends BerlinGroupApiClient<ErstebankConfiguration> {

    ErstebankApiClient(
            final TinkHttpClient client,
            final PersistentStorage persistentStorage,
            final ErstebankConfiguration configuration,
            final CredentialsRequest request,
            final String redirectUrl,
            final String qSealc) {
        super(client, persistentStorage, configuration, request, redirectUrl, qSealc);
    }

    public URL getAuthorizeUrl(final String state) {
        return getAuthorizeUrl(
                        EndPoints.AUTH, state, getConfiguration().getClientId(), getRedirectUrl())
                .getUrl();
    }

    @Override
    public OAuth2Token getToken(final String code) {
        final TokenRequestPost requestPost =
                new TokenRequestPost(
                        FormValues.AUTHORIZATION_CODE,
                        code,
                        getRedirectUrl(),
                        getConfiguration().getClientId(),
                        getConfiguration().getClientSecret());

        return client.request(Urls.TOKEN)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .body(requestPost.toData())
                .post(TokenBaseResponse.class)
                .toTinkToken();
    }

    @Override
    public String getConsentId() {
        return getConsent(Collections.emptyList()).getConsentId();
    }

    public OAuth2Token refreshToken(final String refreshToken) {
        final RefreshTokenRequest request =
                new RefreshTokenRequest(
                        getConfiguration().getClientId(),
                        getConfiguration().getClientSecret(),
                        QueryValues.REFRESH_TOKEN,
                        refreshToken);

        return client.request(Urls.TOKEN)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .queryParams(request.toData())
                .get(TokenBaseResponse.class)
                .toTinkToken();
    }

    @Override
    public ErsteAccountsResponse fetchAccounts() {
        return getAccountsRequestBuilder(EndPoints.ACCOUNTS)
                .header(HeaderKeys.WEB_API_KEY, getConfiguration().getApiKey())
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID())
                .get(ErsteAccountsResponse.class);
    }

    @Override
    public TransactionsKeyPaginatorBaseResponse fetchTransactions(final String url) {
        return null;
    }

    public ErsteKeyPaginatorResponse fetchTransactionsForErste(final String url) {
        return getTransactionsRequestBuilder(url)
                .header(HeaderKeys.WEB_API_KEY, getConfiguration().getApiKey())
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID())
                .get(ErsteKeyPaginatorResponse.class);
    }

    public ConsentBaseResponse getConsent(final List<String> ibans) {

        return buildRequestForIbans(EndPoints.CONSENT, ibans)
                .addBearerToken(getTokenFromSession(StorageKeys.OAUTH_TOKEN))
                .header(HeaderKeys.WEB_API_KEY, getConfiguration().getApiKey())
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID())
                .header(HeaderKeys.PSU_IP_ADDRESS, "127.0.0.1")
                .type(MediaType.APPLICATION_JSON)
                .post(ConsentBaseResponse.class);
    }

    public GetConsentResponse signConsent(final String consentId) {
        return client.request(String.format(EndPoints.SIGN_CONSENT, consentId))
                .accept(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID())
                .header(HeaderKeys.WEB_API_KEY, getConfiguration().getApiKey())
                .addBearerToken(
                        persistentStorage
                                .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                                .orElseThrow(
                                        () ->
                                                new IllegalArgumentException(
                                                        ErrorMessages.MISSING_TOKEN)))
                .get(GetConsentResponse.class);
    }

    public AuthorisationConsentResponse authorizeConsent(
            final String consentID,
            final String authorizationId,
            final String scaAuthenticationData) {

        AuthorisationConsentRequest request =
                new AuthorisationConsentRequest(scaAuthenticationData);

        return client.request(
                        String.format(EndPoints.AUTHORIZE_CONSENT, consentID, authorizationId))
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID())
                .header(HeaderKeys.WEB_API_KEY, getConfiguration().getApiKey())
                .addBearerToken(
                        persistentStorage
                                .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                                .orElseThrow(
                                        () ->
                                                new IllegalArgumentException(
                                                        ErrorMessages.MISSING_TOKEN)))
                .put(AuthorisationConsentResponse.class, request);
    }

    public CreatePaymentResponse createPayment(final CreatePaymentRequest paymentRequest) {
        final URL url = paymentRequest.isSepa() ? Urls.CREATE_SEPA : Urls.CREATE_CROSS_BORDER;

        return buildRequestWithTokenAndConsent(url)
                .body(paymentRequest)
                .post(CreatePaymentResponse.class);
    }

    public FetchPaymentResponse fetchPayment(final PaymentRequest paymentRequest) {
        final URL url =
                paymentRequest.getPayment().isSepa() ? Urls.FETCH_SEPA : Urls.FETCH_CROSS_BORDER;
        final URL urlWithPaymentId =
                url.parameter(IdTags.PAYMENT_ID, paymentRequest.getPayment().getUniqueId());

        return buildRequestWithTokenAndConsent(urlWithPaymentId).get(FetchPaymentResponse.class);
    }

    private RequestBuilder buildRequestWithTokenAndConsent(final URL url) {
        return client.request(url)
                .header(HeaderKeys.WEB_API_KEY, getConfiguration().getApiKey())
                .header(HeaderKeys.CONSENT_ID, persistentStorage.get(StorageKeys.CONSENT_ID))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .addBearerToken(getTokenFromSession(StorageKeys.OAUTH_TOKEN));
    }

    private RequestBuilder buildRequestForIbans(String consent, List<String> ibans) {
        return client.request(consent)
                .body(
                        new ConsentBaseRequest(
                                new AccessEntity.Builder()
                                        .withAccounts(ibans)
                                        .withBalances(ibans)
                                        .withTransactions(ibans)
                                        .build()));
    }
}
