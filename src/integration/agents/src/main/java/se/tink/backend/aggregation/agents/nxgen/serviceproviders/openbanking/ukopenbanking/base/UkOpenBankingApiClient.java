package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base;

import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.rpc.AccountPermissionRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.rpc.AccountPermissionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ProviderConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.SoftwareStatement;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;

public class UkOpenBankingApiClient extends OpenIdApiClient {

    protected final URL apiBaseUrl;
    protected final UkOpenBankingConfig aisConfig;
    private final UkOpenBankingConfig pisConfig;

    public UkOpenBankingApiClient(
            TinkHttpClient httpClient,
            SoftwareStatement softwareStatement,
            ProviderConfiguration providerConfiguration,
            UkOpenBankingConfig aisConfig,
            UkOpenBankingConfig pisConfig,
            OpenIdConstants.ClientMode clientMode) {
        super(httpClient, softwareStatement, providerConfiguration, clientMode);
        apiBaseUrl = providerConfiguration.getApiBaseURL();
        this.aisConfig = aisConfig;
        this.pisConfig = pisConfig;
    }

    public <T> T createPaymentIntentId(Object request, Class<T> responseType) {
        return createRequest(pisConfig.createPaymentsURL(providerConfiguration.getPisConsentURL()))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .header(
                        UkOpenBankingConstants.HttpHeaders.X_IDEMPOTENCY_KEY,
                        RandomUtils.generateRandomHexEncoded(8))
                .body(request)
                .post(responseType);
    }

    public <T> T submitPayment(Object request, Class<T> responseType) {
        return createRequest(
                        pisConfig.createPaymentSubmissionURL(providerConfiguration.getPisBaseURL()))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .header(
                        UkOpenBankingConstants.HttpHeaders.X_IDEMPOTENCY_KEY,
                        RandomUtils.generateRandomHexEncoded(8))
                .body(request)
                .post(responseType);
    }

    public <T extends AccountPermissionResponse> T createAccountIntentId(Class<T> responseType) {
        return createRequest(
                        aisConfig.createConsentRequestURL(providerConfiguration.getAuthBaseURL()))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .body(AccountPermissionRequest.create())
                .post(responseType);
    }

    public <T> T fetchAccounts(Class<T> responseType) {
        return createRequest(aisConfig.getBulkAccountRequestURL(apiBaseUrl)).get(responseType);
    }

    public <T> T fetchAccountBalance(String accountId, Class<T> responseType) {
        return createRequest(aisConfig.getAccountBalanceRequestURL(apiBaseUrl, accountId))
                .get(responseType);
    }

    public <T> T fetchAccountTransactions(String paginationKey, Class<T> responseType) {

        // Check if the key provided is a complete url or if it should be appended on the apiBase
        URL url = new URL(paginationKey);
        if (url.getScheme() == null) url = apiBaseUrl.concat(paginationKey);

        return createRequest(url).get(responseType);
    }

    public <T> T fetchUpcomingTransactions(String accountId, Class<T> responseType) {
        try {

            return createRequest(aisConfig.getUpcomingTransactionRequestURL(apiBaseUrl, accountId))
                    .get(responseType);
        } catch (Exception e) {
            // TODO: Ukob testdata has an error in it which makes some transactions impossible to
            // parse.
            // TODO: This combined with the null check in UpcomingTransactionFetcher discards those
            // transactions to prevents crash.
            return null;
        }
    }

    public UkOpenBankingConfig getAisConfig() {
        return aisConfig;
    }

    private RequestBuilder createRequest(URL url) {
        return httpClient.request(url).accept(MediaType.APPLICATION_JSON_TYPE);
    }

    public String fetchIntentIdString() {
        return this.getAisConfig()
                .getIntentId(this.createAccountIntentId(aisConfig.getIntentIdResponseType()));
    }

    // Payments revamp

    private RequestBuilder createPISRequest(URL url) {
        return createRequest(url)
                .header(
                        UkOpenBankingConstants.HttpHeaders.X_IDEMPOTENCY_KEY,
                        RandomUtils.generateRandomHexEncoded(8));
    }

    public <T> T createDomesticPaymentConsent(Object request, Class<T> responseType) {
        return createPISRequest(pisConfig.createDomesticPaymentConsentURL(apiBaseUrl))
                .post(responseType, request);
    }

    public <T> T getDomesticPaymentConsent(String consentId, Class<T> responseType) {
        return createPISRequest(pisConfig.getDomesticPaymentConsentURL(apiBaseUrl, consentId))
                .get(responseType);
    }

    public <T> T executeDomesticPayment(Object request, Class<T> responseType) {
        return createPISRequest(pisConfig.createDomesticPaymentURL(apiBaseUrl))
                .post(responseType, request);
    }

    public <T> T getDomesticFundsConfirmation(String consentId, Class<T> responseType) {
        return createPISRequest(pisConfig.getDomesticFundsConfirmationURL(apiBaseUrl, consentId))
                .get(responseType);
    }

    public <T> T getDomesticPayment(String paymentId, Class<T> responseType) {
        return createPISRequest(pisConfig.getDomesticPayment(apiBaseUrl, paymentId))
                .get(responseType);
    }

    public <T> T createInternationalPaymentConsent(Object request, Class<T> responseType) {
        return createPISRequest(pisConfig.createInternationalPaymentConsentURL(apiBaseUrl))
                .post(responseType, request);
    }

    public <T> T getInternationalPaymentConsent(String consentId, Class<T> responseType) {
        return createPISRequest(pisConfig.getInternationalPaymentConsentURL(apiBaseUrl, consentId))
                .get(responseType);
    }

    public <T> T getInternationalPayment(String consentId, Class<T> responseType) {
        return createPISRequest(pisConfig.getInternationalPayment(apiBaseUrl, consentId))
                .post(responseType, consentId);
    }

    public <T> T getInternationalFundsConfirmation(String consentId, Class<T> responseType) {
        return createPISRequest(
                        pisConfig.getInternationalFundsConfirmationURL(apiBaseUrl, consentId))
                .get(responseType);
    }

    public <T> T executeInternationalPayment(Object request, Class<T> responseType) {
        return createPISRequest(pisConfig.createInternationalPaymentURL(apiBaseUrl))
                .post(responseType, request);
    }
}
