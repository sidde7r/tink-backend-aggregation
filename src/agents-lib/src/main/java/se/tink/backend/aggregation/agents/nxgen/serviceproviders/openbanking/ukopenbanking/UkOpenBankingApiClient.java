package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking;

import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.authenticator.rpc.AccountPermissionRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.authenticator.rpc.AccountPermissionResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ProviderConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.SoftwareStatement;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

public class UkOpenBankingApiClient extends OpenIdApiClient {

    public UkOpenBankingApiClient(TinkHttpClient httpClient, SoftwareStatement softwareStatement,
            ProviderConfiguration providerConfiguration) {
        super(httpClient, softwareStatement, providerConfiguration);
    }

    public AccountPermissionResponse createAccountIntentId() {
        return httpClient.request(providerConfiguration.getAccountRequestsURL())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .body(AccountPermissionRequest.create())
                .post(AccountPermissionResponse.class);
    }

    public <T> T fetchAccounts(Class<T> responseType) {
        return httpClient.request(providerConfiguration.getFetchAccountURL())
                .get(responseType);
    }

    public <T> T fetchAccountBalance(Class<T> responseType) {
        return httpClient.request(providerConfiguration.getFetchAccountBalancleURL())
                .get(responseType);
    }

    public <T> T fetchAccountTransactions(String paginationKey, Class<T> responseType) {
        return httpClient.request(providerConfiguration.getApiBaseURL().concat(paginationKey))
                .get(responseType);
    }
}
