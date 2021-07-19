package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.accounts;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Headers.HeaderKeys.X_REQUEST_ID;

import java.time.ZonedDateTime;
import javax.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.accounts.dto.requests.accountdetails.AccountDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.accounts.dto.requests.accounts.AccountsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.accounts.dto.responses.accountdetails.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.accounts.dto.responses.accounts.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.common.BasePolishApiPostClient;
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
public class PolishApiPostAccountClient extends BasePolishApiPostClient
        implements PolishApiAccountClient {

    private final PolishAccountsApiUrlFactory urlFactory;

    public PolishApiPostAccountClient(
            PolishApiAgentCreator polishApiAgentCreator,
            TinkHttpClient httpClient,
            AgentConfiguration<PolishApiConfiguration> configuration,
            AgentComponentProvider agentComponentProvider,
            PolishApiPersistentStorage persistentStorage) {
        super(
                httpClient,
                agentComponentProvider,
                configuration,
                persistentStorage,
                polishApiAgentCreator);
        this.urlFactory = polishApiAgentCreator.getAccountApiUrlFactory();
    }

    @Override
    public AccountsResponse fetchAccounts() {
        if (persistentStorage.getAccounts().isPresent()) {
            log.info("[Polish API] Accounts - Accounts available in storage");
            return persistentStorage.getAccounts().get();
        }

        String requestId = getUuid();
        ZonedDateTime zonedDateTime = getNow();
        AccountsRequest accountsRequest =
                AccountsRequest.builder()
                        .requestHeader(
                                getRequestHeaderEntity(
                                        requestId, zonedDateTime, getAccessTokenFromStorage()))
                        .perPage(100)
                        .build();

        RequestBuilder requestBuilder =
                getRequestWithBaseHeaders(
                                urlFactory.getAccountsUrl(), zonedDateTime, getTokenFromStorage())
                        .header(X_REQUEST_ID, requestId)
                        .body(accountsRequest, MediaType.APPLICATION_JSON);

        AccountsResponse accountsResponse =
                PolishApiErrorHandler.callWithErrorHandling(
                        requestBuilder,
                        AccountsResponse.class,
                        PolishApiErrorHandler.RequestType.POST);
        persistentStorage.persistAccounts(accountsResponse);
        return accountsResponse;
    }

    @Override
    public AccountDetailsResponse fetchAccountDetails(String accountIdentifier) {
        String requestId = getUuid();
        ZonedDateTime zonedDateTime = getNow();
        AccountDetailsRequest accountDetailsRequest =
                AccountDetailsRequest.builder()
                        .requestHeader(
                                getRequestHeaderEntity(
                                        requestId, zonedDateTime, getAccessTokenFromStorage()))
                        .accountNumber(accountIdentifier)
                        .build();

        RequestBuilder requestBuilder =
                getRequestWithBaseHeaders(
                                urlFactory.getAccountDetailsUrl(accountIdentifier),
                                zonedDateTime,
                                getTokenFromStorage())
                        .header(X_REQUEST_ID, requestId)
                        .body(accountDetailsRequest, MediaType.APPLICATION_JSON);

        return PolishApiErrorHandler.callWithErrorHandling(
                requestBuilder,
                AccountDetailsResponse.class,
                PolishApiErrorHandler.RequestType.POST);
    }
}
