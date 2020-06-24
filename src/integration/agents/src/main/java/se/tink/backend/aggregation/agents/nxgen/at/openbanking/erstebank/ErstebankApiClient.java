package se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank;

import java.util.Collections;
import java.util.List;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang.NotImplementedException;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.ErstebankConstants.EndPoints;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.ErstebankConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.ErstebankConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.authenticator.rpc.ConsentSignResponse;
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
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.Signature;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity.AuthorizationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.ConsentBaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.TokenBaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.TokenRequestGet;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc.AccountsBaseResponseBerlinGroup;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc.TransactionsKeyPaginatorBaseResponse;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.date.DateFormat;

public final class ErstebankApiClient extends BerlinGroupApiClient<ErstebankConfiguration> {

    ErstebankApiClient(
            final TinkHttpClient client,
            final SessionStorage sessionStorage,
            final ErstebankConfiguration configuration) {
        super(client, sessionStorage, configuration);
    }

    public URL getAuthorizeUrl(final String state) {
        return getAuthorizeUrl(
                        EndPoints.AUTH,
                        state,
                        getConfiguration().getClientId(),
                        getConfiguration().getRedirectUrl())
                .getUrl();
    }

    @Override
    public OAuth2Token getToken(final String code) {
        final TokenRequestGet request =
                new TokenRequestGet(
                        getConfiguration().getClientId(),
                        getConfiguration().getRedirectUrl(),
                        code,
                        QueryValues.AUTHORIZATION_CODE);

        return client.request(Urls.TOKEN)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .queryParams(request.toData())
                .get(TokenBaseResponse.class)
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
    public AccountsBaseResponseBerlinGroup fetchAccounts() {
        return getAccountsRequestBuilder(EndPoints.ACCOUNTS)
                .header(HeaderKeys.WEB_API_KEY, getConfiguration().getApiKey())
                .get(AccountsBaseResponseBerlinGroup.class);
    }

    @Override
    public TransactionsKeyPaginatorBaseResponse fetchTransactions(final String url) {
        return getTransactionsRequestBuilder(url)
                .header(HeaderKeys.WEB_API_KEY, getConfiguration().getApiKey())
                .get(TransactionsKeyPaginatorBaseResponse.class);
    }

    public ConsentBaseResponse getConsent(final List<String> ibans) {
        return new ConsentBaseResponse("received", "1234-wertiq-983");

        //         TODO: Wait for erste's api to be fixed
        //
        //        return buildRequestForIbans(Urls.CONSENT, ibans)
        //                .addBearerToken(getTokenFromSession(StorageKeys.OAUTH_TOKEN))
        //                .header(HeaderKeys.WEB_API_KEY, getConfiguration().getApiKey())
        //                .type(MediaType.APPLICATION_JSON)
        //                .post(ConsentBaseResponse.class);
    }

    public ConsentSignResponse signConsent(final String consentId) {
        return buildRequestWithSignature(
                        String.format(EndPoints.SIGN_CONSENT, consentId), FormValues.EMPTY)
                .header(HeaderKeys.WEB_API_KEY, getConfiguration().getApiKey())
                .type(MediaType.APPLICATION_JSON)
                .post(ConsentSignResponse.class);
    }

    private String getSignature(final String digest, final String requestId) {
        throw new NotImplementedException();
    }

    private String getAuthorization(final String digest, final String requestId) {
        final String clientId = getConfiguration().getClientId();

        return new AuthorizationEntity(clientId, getSignature(digest, requestId)).toString();
    }

    private String generateDigest(final String data) {
        return Signature.DIGEST_PREFIX + Psd2Headers.calculateDigest(data);
    }

    private String getFormattedDate() {
        return DateFormat.getFormattedCurrentDate(Signature.DATE_FORMAT, Signature.TIMEZONE);
    }

    private RequestBuilder buildRequest(
            final String date, final String digest, final String reqPath) {

        return client.request(reqPath)
                .accept(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.DIGEST, digest)
                .header(HeaderKeys.DATE, date);
    }

    // Can't test it, since erste's api is not checking for signature
    private RequestBuilder buildRequestWithSignature(final String reqPath, final String payload) {
        final String reqId = Psd2Headers.getRequestId();
        final String date = getFormattedDate();
        final String digest = generateDigest(payload);

        return buildRequest(date, digest, reqPath)
                .header(HeaderKeys.SIGNATURE, getAuthorization(digest, reqId))
                .header(HeaderKeys.CONSENT_ID, getConsentId())
                .addBearerToken(
                        sessionStorage
                                .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                                .orElseThrow(
                                        () ->
                                                new IllegalStateException(
                                                        ErrorMessages.MISSING_TOKEN)));
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
                .header(HeaderKeys.CONSENT_ID, sessionStorage.get(StorageKeys.CONSENT_ID))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .addBearerToken(getTokenFromSession(StorageKeys.OAUTH_TOKEN));
    }
}
