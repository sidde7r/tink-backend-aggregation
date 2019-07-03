package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base;

import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.rpc.AccountPermissionRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.rpc.AccountPermissionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingPisConfig;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ProviderConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.SoftwareStatement;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;

public class UkOpenBankingApiClient extends OpenIdApiClient {

    public UkOpenBankingApiClient(
            TinkHttpClient httpClient,
            SoftwareStatement softwareStatement,
            ProviderConfiguration providerConfiguration,
            OpenIdConstants.ClientMode clientMode,
            URL wellKnownURL) {
        super(httpClient, softwareStatement, providerConfiguration, clientMode, wellKnownURL);
        this.httpClient.setDebugOutput(true);
    }

    public <T> T createPaymentIntentId(
            UkOpenBankingPisConfig pisConfig, Object request, Class<T> responseType) {
        return createRequest(pisConfig.createPaymentsURL())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .header(
                        UkOpenBankingConstants.HttpHeaders.X_IDEMPOTENCY_KEY,
                        RandomUtils.generateRandomHexEncoded(8))
                .body(request)
                .post(responseType);
    }

    public <T> T submitPayment(
            UkOpenBankingPisConfig pisConfig, Object request, Class<T> responseType) {
        return createRequest(pisConfig.createPaymentSubmissionURL())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .header(
                        UkOpenBankingConstants.HttpHeaders.X_IDEMPOTENCY_KEY,
                        RandomUtils.generateRandomHexEncoded(8))
                .body(request)
                .post(responseType);
    }

    private <T extends AccountPermissionResponse> T createAccountIntentId(
            UkOpenBankingAisConfig aisConfig, Class<T> responseType) {

        return createRequest(aisConfig.createConsentRequestURL())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .body(AccountPermissionRequest.create())
                .post(responseType);
    }

    public <T> T fetchAccounts(UkOpenBankingAisConfig aisConfig, Class<T> responseType) {
        return createRequest(aisConfig.getBulkAccountRequestURL()).get(responseType);
    }

    public <T> T fetchAccountBalance(
            UkOpenBankingAisConfig aisConfig, String accountId, Class<T> responseType) {
        return createRequest(aisConfig.getAccountBalanceRequestURL(accountId)).get(responseType);
    }

    public <T> T fetchAccountTransactions(
            UkOpenBankingAisConfig aisConfig, String paginationKey, Class<T> responseType) {

        // Check if the key provided is a complete url or if it should be appended on the apiBase
        URL url = new URL(paginationKey);
        if (url.getScheme() == null) url = aisConfig.getApiBaseURL().concat(paginationKey);

        return createRequest(url).get(responseType);
    }

    public <T> T fetchUpcomingTransactions(
            UkOpenBankingAisConfig aisConfig, String accountId, Class<T> responseType) {
        try {

            return createRequest(aisConfig.getUpcomingTransactionRequestURL(accountId))
                    .get(responseType);
        } catch (Exception e) {
            // TODO: Ukob testdata has an error in it which makes some transactions impossible to
            // parse.
            // TODO: This combined with the null check in UpcomingTransactionFetcher discards those
            // transactions to prevents crash.
            return null;
        }
    }

    private RequestBuilder createRequest(URL url) {
        return httpClient.request(url).accept(MediaType.APPLICATION_JSON_TYPE);
    }

    public String fetchIntentIdString(UkOpenBankingAisConfig aisConfig) {
        return aisConfig.getIntentId(
                this.createAccountIntentId(aisConfig, aisConfig.getIntentIdResponseType()));
    }

    // General Payments Interface

    private RequestBuilder createPISRequest(URL url) {
        return createRequest(url)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .header(
                        UkOpenBankingConstants.HttpHeaders.X_IDEMPOTENCY_KEY,
                        RandomUtils.generateRandomHexEncoded(8));
    }

    public <T> T createDomesticPaymentConsent(
            UkOpenBankingPisConfig pisConfig, Object request, Class<T> responseType) {
        return createPISRequest(pisConfig.createDomesticPaymentConsentURL())
                .post(responseType, request);
    }

    public <T> T getDomesticPaymentConsent(
            UkOpenBankingPisConfig pisConfig, String consentId, Class<T> responseType) {
        return createPISRequest(pisConfig.getDomesticPaymentConsentURL(consentId))
                .get(responseType);
    }

    public <T> T executeDomesticPayment(
            UkOpenBankingPisConfig pisConfig, Object request, Class<T> responseType) {
        return createPISRequest(pisConfig.createDomesticPaymentURL()).post(responseType, request);
    }

    public <T> T getDomesticFundsConfirmation(
            UkOpenBankingPisConfig pisConfig, String consentId, Class<T> responseType) {
        return createPISRequest(pisConfig.getDomesticFundsConfirmationURL(consentId))
                .get(responseType);
    }

    public <T> T getDomesticPayment(
            UkOpenBankingPisConfig pisConfig, String paymentId, Class<T> responseType) {
        return createPISRequest(pisConfig.getDomesticPayment(paymentId)).get(responseType);
    }

    public <T> T createInternationalPaymentConsent(
            UkOpenBankingPisConfig pisConfig, Object request, Class<T> responseType) {
        return createPISRequest(pisConfig.createInternationalPaymentConsentURL())
                .post(responseType, request);
    }

    public <T> T getInternationalPaymentConsent(
            UkOpenBankingPisConfig pisConfig, String consentId, Class<T> responseType) {
        return createPISRequest(pisConfig.getInternationalPaymentConsentURL(consentId))
                .get(responseType);
    }

    public <T> T getInternationalPayment(
            UkOpenBankingPisConfig pisConfig, String consentId, Class<T> responseType) {
        return createPISRequest(pisConfig.getInternationalPayment(consentId))
                .post(responseType, consentId);
    }

    public <T> T getInternationalFundsConfirmation(
            UkOpenBankingPisConfig pisConfig, String consentId, Class<T> responseType) {
        return createPISRequest(pisConfig.getInternationalFundsConfirmationURL(consentId))
                .get(responseType);
    }

    public <T> T executeInternationalPayment(
            UkOpenBankingPisConfig pisConfig, Object request, Class<T> responseType) {
        return createPISRequest(pisConfig.createInternationalPaymentURL())
                .post(responseType, request);
    }
}
