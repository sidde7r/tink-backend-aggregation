package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31;

import java.security.SecureRandom;
import java.util.Random;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.rpc.AccountPermissionRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.authenticator.rpc.AccountPermissionResponseV31;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ProviderConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.SoftwareStatement;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;

public class UkOpenBankingV31ApiClient extends UkOpenBankingApiClient {
    private static final Random random = new SecureRandom();

    public UkOpenBankingV31ApiClient(
            TinkHttpClient httpClient,
            SoftwareStatement softwareStatement,
            ProviderConfiguration providerConfiguration,
            UkOpenBankingConfig aisConfig,
            UkOpenBankingConfig pisConfig,
            OpenIdConstants.ClientMode clientMode) {
        super(httpClient, softwareStatement, providerConfiguration, aisConfig, pisConfig, clientMode);
    }

    public <T> T createPaymentIntentId(Object request, Class<T> responseType) {
        return createRequest(aisConfig.createPaymentsURL(providerConfiguration.getPisConsentURL()))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .header(
                        UkOpenBankingConstants.HttpHeaders.X_IDEMPOTENCY_KEY,
                        RandomUtils.generateRandomHexEncoded(8))
                .body(request)
                .post(responseType);
    }

    public <T> T submitPayment(Object request, Class<T> responseType) {
        return createRequest(aisConfig.createPaymentSubmissionURL(providerConfiguration.getPisBaseURL()))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .header(
                        UkOpenBankingConstants.HttpHeaders.X_IDEMPOTENCY_KEY,
                        RandomUtils.generateRandomHexEncoded(8))
                .body(request)
                .post(responseType);
    }

    @Override
    public AccountPermissionResponseV31 createAccountIntentId() {
        HttpResponse post =
                createRequest(aisConfig.createConsentRequestURL(providerConfiguration.getAuthBaseURL()))
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .body(AccountPermissionRequest.create())
                        .post(HttpResponse.class);
        return post.getBody(AccountPermissionResponseV31.class);
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
}
