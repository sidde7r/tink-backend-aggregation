package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink;

import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity.AccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity.AuthorizationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity.SignatureEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.ConsentBaseRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.ConsentBaseResponseWithoutHref;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.TokenBaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.TokenRequestPost;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc.AccountsBaseResponseBerlinGroup;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc.TransactionsKeyPaginatorBaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.SamlinkConstants.BookingStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.SamlinkConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.SamlinkConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.configuration.SamlinkConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.payment.rpc.FetchPaymentResponse;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class SamlinkApiClient extends BerlinGroupApiClient<SamlinkConfiguration> {

    private final QsealcSigner qsealcSigner;

    public SamlinkApiClient(
            final TinkHttpClient client,
            final PersistentStorage persistentStorage,
            final QsealcSigner qsealcSigner,
            final SamlinkConfiguration configuration,
            final String redirectUrl,
            final String qSealc) {
        super(client, persistentStorage, configuration, redirectUrl, qSealc);

        this.qsealcSigner = qsealcSigner;
    }

    public URL getAuthorizeUrl(final String state) {
        final String consentId = getConsentId();
        persistentStorage.put(StorageKeys.CONSENT_ID, consentId);
        final String authUrl = getConfiguration().getBaseUrl() + Urls.AUTH;
        return getAuthorizeUrlWithCode(
                        authUrl,
                        state,
                        consentId,
                        getConfiguration().getClientId(),
                        getRedirectUrl())
                .getUrl();
    }

    // The "withBalance" URL parameter is not supported by SamLink
    @Override
    public AccountsBaseResponseBerlinGroup fetchAccounts() {
        return buildRequestWithSignature(getConfiguration().getBaseUrl() + Urls.ACCOUNTS, "")
                .get(AccountsBaseResponseBerlinGroup.class);
    }

    private RequestBuilder buildRequestWithSignature(final String url, final String body) {
        final String digest = generateDigest(body);
        final String requestId = UUID.randomUUID().toString();
        final OAuth2Token token = getTokenFromSession(StorageKeys.OAUTH_TOKEN);

        return client.request(url)
                .header(SamlinkConstants.HeaderKeys.SIGNATURE, getAuthorization(digest, requestId))
                .addBearerToken(token)
                .header(BerlinGroupConstants.HeaderKeys.X_REQUEST_ID, requestId)
                .header(HeaderKeys.DIGEST, digest)
                .header(HeaderKeys.TPP_REDIRECT_URI, getRedirectUrl())
                .header(
                        SamlinkConstants.HeaderKeys.TPP_SIGNATURE_CERTIFICATE,
                        getConfiguration().getCertificate())
                .header(HeaderKeys.CONSENT_ID, persistentStorage.get(StorageKeys.CONSENT_ID))
                .header(
                        SamlinkConstants.HeaderKeys.SUBSCRIPTION_KEY,
                        getConfiguration().getSubscriptionKey());
    }

    // The bookingStatus parameter "both" is not supported by Samlink
    @Override
    public TransactionsKeyPaginatorBaseResponse fetchTransactions(final String url) {
        return buildRequestWithSignature(url, "")
                .queryParam(QueryKeys.BOOKING_STATUS, BookingStatus.PENDING)
                .get(TransactionsKeyPaginatorBaseResponse.class);
    }

    @Override
    public OAuth2Token getToken(final String code) {
        final TokenRequestPost tokenRequest =
                new TokenRequestPost(
                        FormValues.AUTHORIZATION_CODE,
                        code,
                        getRedirectUrl(),
                        getConfiguration().getClientId(),
                        getConfiguration().getClientSecret(),
                        "");

        return createRequest(new URL(getConfiguration().getBaseUrl() + Urls.TOKEN))
                .body(tokenRequest.toData(), MediaType.APPLICATION_FORM_URLENCODED)
                .addBasicAuth(
                        getConfiguration().getClientId(), getConfiguration().getClientSecret())
                .post(TokenBaseResponse.class)
                .toTinkToken();
    }

    @Override
    public String getConsentId() {
        AccessEntity accessEntity = new AccessEntity.Builder().build();
        final ConsentBaseRequest consentsRequest = new ConsentBaseRequest();
        consentsRequest.setAccess(accessEntity);

        return createRequest(new URL(getConfiguration().getBaseUrl() + Urls.CONSENT))
                .body(consentsRequest.toData(), MediaType.APPLICATION_JSON_TYPE)
                .post(ConsentBaseResponseWithoutHref.class)
                .getConsentId();
    }

    @Override
    public OAuth2Token refreshToken(final String token) {
        return null;
    }

    private RequestBuilder createRequest(final URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(
                        SamlinkConstants.HeaderKeys.SUBSCRIPTION_KEY,
                        getConfiguration().getSubscriptionKey())
                .type(MediaType.APPLICATION_JSON);
    }

    private String getAuthorization(final String digest, String requestId) {
        return new AuthorizationEntity(
                        getConfiguration().getKeyId(), getSignature(digest, requestId))
                .toString();
    }

    private String generateDigest(final String data) {
        return SamlinkConstants.HeaderKeys.DIGEST_PREFIX + Psd2Headers.calculateDigest(data);
    }

    private String getSignature(final String digest, String requestId) {
        final SignatureEntity signatureEntity = new SignatureEntity(digest, requestId);

        return qsealcSigner.getSignatureBase64(signatureEntity.toString().getBytes());
    }

    public CreatePaymentResponse createSepaPayment(CreatePaymentRequest paymentRequest) {
        return createRequest(
                        new URL(getConfiguration().getBaseUrl()).concat(Urls.CREATE_SEPA_PAYMENT))
                .body(paymentRequest)
                .post(CreatePaymentResponse.class);
    }

    public CreatePaymentResponse createForeignPayment(CreatePaymentRequest paymentRequest) {
        return createRequest(
                        new URL(getConfiguration().getBaseUrl())
                                .concat(Urls.CREATE_FOREIGN_PAYMENT))
                .body(paymentRequest)
                .post(CreatePaymentResponse.class);
    }

    public FetchPaymentResponse fetchSepaPayment(PaymentRequest paymentRequest) {
        URL urlWithPaymentId =
                new URL(getConfiguration().getBaseUrl())
                        .concat(Urls.GET_SEPA_PAYMENT)
                        .parameter(IdTags.PAYMENT_ID, paymentRequest.getPayment().getUniqueId());
        return fetchPayment(urlWithPaymentId);
    }

    public FetchPaymentResponse fetchForeignPayment(PaymentRequest paymentRequest) {
        URL urlWithPaymentId =
                new URL(getConfiguration().getBaseUrl())
                        .concat(Urls.GET_FOREIGN_PAYMENT)
                        .parameter(IdTags.PAYMENT_ID, paymentRequest.getPayment().getUniqueId());
        return fetchPayment(urlWithPaymentId);
    }

    private FetchPaymentResponse fetchPayment(URL url) {
        return createRequest(url)
                .addBearerToken(getTokenFromSession(StorageKeys.OAUTH_TOKEN))
                .get(FetchPaymentResponse.class);
    }
}
