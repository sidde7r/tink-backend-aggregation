package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc;

import javax.ws.rs.core.MediaType;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.KbcConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity.AccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity.AccessEntityBerlinGroup;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.ConsentBaseRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.ConsentBaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.RefreshTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.TokenBaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.TokenRequestPost;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.executor.payment.enums.BerlinGroupPaymentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.executor.payment.rpc.GetPaymentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc.AccountsBaseResponseBerlinGroup;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc.TransactionsKeyPaginatorBaseResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public final class KbcApiClient extends BerlinGroupApiClient {

    private final Credentials credentials;

    public KbcApiClient(
            final TinkHttpClient client,
            final SessionStorage sessionStorage,
            final Credentials credentials) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.credentials = credentials;
    }

    @Override
    public AccountsBaseResponseBerlinGroup fetchAccounts() {
        return getAccountsRequestBuilder(getConfiguration().getBaseUrl() + Urls.ACCOUNTS)
                .get(AccountsBaseResponseBerlinGroup.class);
    }

    @Override
    public TransactionsKeyPaginatorBaseResponse fetchTransactions(final String url) {
        return getTransactionsRequestBuilder(
                        getConfiguration().getBaseUrl() + Urls.AIS_PRODUCT + url)
                .get(TransactionsKeyPaginatorBaseResponse.class);
    }

    public URL getAuthorizeUrl(final String state) {
        final String consentId = getConsentId();
        sessionStorage.put(StorageKeys.CONSENT_ID, consentId);
        final String authUrl = getConfiguration().getBaseUrl() + Urls.AUTH;
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
                .body(refreshTokenRequest.toData(), MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenBaseResponse.class)
                .toTinkToken();
    }

    @SuppressWarnings("Duplicates")
    @Override
    public String getConsentId() {
        final AccessEntity accessEntity = new AccessEntityBerlinGroup();
        accessEntity.addIban(credentials.getField("IBAN"));
        final ConsentBaseRequest consentsRequest = new ConsentBaseRequest();
        consentsRequest.setAccess(accessEntity);

        return client.request(getConfiguration().getBaseUrl() + Urls.CONSENT)
                .body(consentsRequest.toData(), MediaType.APPLICATION_JSON_TYPE)
                .post(ConsentBaseResponse.class)
                .getConsentId();
    }

    public CreatePaymentResponse createPayment(
            CreatePaymentRequest createPaymentRequest, BerlinGroupPaymentType paymentType) {
        return getPaymentRequestBuilder(
                        new URL(getConfiguration().getBaseUrl() + Urls.PAYMENTS)
                                .parameter(IdTags.PAYMENT_PRODUCT, paymentType.toString()))
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
}
