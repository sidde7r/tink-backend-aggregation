package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking;

import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.authenticator.rpc.AccountPermissionRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.authenticator.rpc.AccountPermissionResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ProviderConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.SoftwareStatement;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;

public class UkOpenBankingApiClient extends OpenIdApiClient {

    private final URL apiBaseUrl;

    public UkOpenBankingApiClient(TinkHttpClient httpClient, SoftwareStatement softwareStatement,
            ProviderConfiguration providerConfiguration) {
        super(httpClient, softwareStatement, providerConfiguration);
        apiBaseUrl = providerConfiguration.getApiBaseURL();
    }

    public AccountPermissionResponse createAccountIntentId() {
        return httpClient.request(providerConfiguration.getAccountRequestsURL())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .body(AccountPermissionRequest.create())
                .post(AccountPermissionResponse.class);
    }

    public <T> T fetchAccounts(Class<T> responseType) {
        return httpClient.request(
                UkOpenBankingConstants.ApiServices.getBulkAccountRequestURL(apiBaseUrl))
                .get(responseType);
    }

    public <T> T fetchAccountBalance(String accountId, Class<T> responseType) {
        return httpClient.request(
                UkOpenBankingConstants.ApiServices.getAccountBalanceRequestURL(apiBaseUrl, accountId))
                .get(responseType);
    }

    public <T> T fetchAccountTransactions(String paginationKey, Class<T> responseType) {
        return httpClient.request(apiBaseUrl.concat(paginationKey))
                .get(responseType);
    }

    public <T> T fetchUpcomingTransactions(String accountId, Class<T> responseType) {
        try {

            return httpClient
                    .request(UkOpenBankingConstants.ApiServices.getUpcomingTransactionRequestURL(apiBaseUrl, accountId))
                    .get(responseType);
        } catch (Exception e) {
            // TODO: Ukob testdata has an error in it which makes some transactions impossible to parse.
            // TODO: This combined with the null check in UpcomingTransactionFetcher discards those transactions to prevents crash.
            return null;
        }
    }
}
