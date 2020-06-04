package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.samlink;

import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.samlink.SamlinkConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.samlink.SamlinkConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.samlink.configuration.SamlinkConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.samlink.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.samlink.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.samlink.executor.payment.rpc.FetchPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity.AccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.ConsentBaseRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.ConsentBaseResponseWithoutHref;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.TokenBaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.TokenRequestPost;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc.AccountsBaseResponseBerlinGroup;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc.TransactionsKeyPaginatorBaseResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public final class SamlinkApiClient extends BerlinGroupApiClient<SamlinkConfiguration> {

    public SamlinkApiClient(
            final TinkHttpClient client,
            final SessionStorage sessionStorage,
            final SamlinkConfiguration configuration,
            final String redirectUrl) {
        super(client, sessionStorage, configuration, redirectUrl);
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
                        getRedirectUrl())
                .getUrl();
    }

    @Override
    public AccountsBaseResponseBerlinGroup fetchAccounts() {
        return getAccountsRequestBuilder(getConfiguration().getBaseUrl() + Urls.ACCOUNTS)
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(
                        SamlinkConstants.HeaderKeys.SUBSCRIPTION_KEY,
                        getConfiguration().getSubscriptionKey())
                .get(AccountsBaseResponseBerlinGroup.class);
    }

    @Override
    public TransactionsKeyPaginatorBaseResponse fetchTransactions(final String url) {
        return createRequest(new URL(url)).get(TransactionsKeyPaginatorBaseResponse.class);
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
