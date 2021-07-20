package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.accounts;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Logs.LOG_TAG;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.accounts.dto.responses.accountdetails.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.accounts.dto.responses.accounts.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.common.BasePolishApiGetClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.concreteagents.PolishApiAgentCreator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.urlfactory.PolishAccountsApiUrlFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.errorhandling.PolishApiErrorHandler;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;

@Slf4j
public class PolishApiGetAccountClient extends BasePolishApiGetClient
        implements PolishApiAccountClient {

    private final PolishAccountsApiUrlFactory urlFactory;

    public PolishApiGetAccountClient(
            PolishApiAgentCreator apiAgentCreator,
            TinkHttpClient httpClient,
            AgentConfiguration<PolishApiConfiguration> configuration,
            AgentComponentProvider agentComponentProvider,
            PolishApiPersistentStorage persistentStorage) {
        super(httpClient, configuration, agentComponentProvider, persistentStorage);
        this.urlFactory = apiAgentCreator.getAccountApiUrlFactory();
    }

    @Override
    public AccountsResponse fetchAccounts() {
        if (persistentStorage.getAccounts().isPresent()) {
            log.info("{} Accounts - Accounts available in storage", LOG_TAG);
            return persistentStorage.getAccounts().get();
        }

        RequestBuilder requestBuilder =
                getRequestWithBaseHeaders(urlFactory.getAccountsUrl(), getTokenFromStorage());

        AccountsResponse accountsResponse =
                PolishApiErrorHandler.callWithErrorHandling(
                        requestBuilder,
                        AccountsResponse.class,
                        PolishApiErrorHandler.RequestType.GET);
        persistentStorage.persistAccounts(accountsResponse);
        return accountsResponse;
    }

    @Override
    public AccountDetailsResponse fetchAccountDetails(String accountIdentifier) {
        RequestBuilder requestBuilder =
                getRequestWithBaseHeaders(
                        urlFactory.getAccountDetailsUrl(accountIdentifier), getTokenFromStorage());

        return PolishApiErrorHandler.callWithErrorHandling(
                requestBuilder,
                AccountDetailsResponse.class,
                PolishApiErrorHandler.RequestType.GET);
    }
}
