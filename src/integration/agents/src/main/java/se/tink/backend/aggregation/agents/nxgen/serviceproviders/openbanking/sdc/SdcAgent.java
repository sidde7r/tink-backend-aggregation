package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.SAVINGS_ACCOUNTS;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcConstants.Transactions.MONTHS_TO_FETCH;

import com.google.inject.Inject;
import java.time.temporal.ChronoUnit;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.authenticator.SdcAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.authenticator.SdcAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.configuration.SdcConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.fetcher.transactionalaccount.SdcTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.fetcher.transactionalaccount.SdcTransactionalAccountTransactionFetcher;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS})
public class SdcAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final SdcApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    @Inject
    public SdcAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);

        apiClient = constructApiClient();
        transactionalAccountRefreshController =
                getTransactionalAccountRefreshController(
                        componentProvider.getCredentialsRequest().getProvider().getMarket(),
                        componentProvider.getLocalDateTimeSource());
    }

    protected AgentConfiguration<SdcConfiguration> getAgentConfiguration() {
        return getAgentConfigurationController().getAgentConfiguration(SdcConfiguration.class);
    }

    @Override
    public void setConfiguration(final AgentsServiceConfiguration agentsServiceConfiguration) {
        super.setConfiguration(agentsServiceConfiguration);
        client.setEidasProxy(agentsServiceConfiguration.getEidasProxy());
    }

    protected SdcApiClient constructApiClient() {
        return new SdcApiClient(
                client,
                new SdcUrlProvider(),
                persistentStorage,
                getAgentConfiguration().getProviderSpecificConfiguration(),
                getAgentConfiguration().getRedirectUrl());
    }

    @Override
    protected Authenticator constructAuthenticator() {

        final SdcAuthenticationController controller =
                new SdcAuthenticationController(
                        supplementalInformationHelper,
                        new SdcAuthenticator(apiClient, persistentStorage),
                        strongAuthenticationState,
                        credentials);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        controller, supplementalInformationHelper),
                controller);
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return transactionalAccountRefreshController.fetchCheckingAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return transactionalAccountRefreshController.fetchCheckingTransactions();
    }

    @Override
    public FetchAccountsResponse fetchSavingsAccounts() {
        return transactionalAccountRefreshController.fetchSavingsAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchSavingsTransactions() {
        return transactionalAccountRefreshController.fetchSavingsTransactions();
    }

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController(
            String providerMarket, LocalDateTimeSource localDateTimeSource) {
        final SdcTransactionalAccountFetcher accountFetcher =
                new SdcTransactionalAccountFetcher(apiClient);

        final SdcTransactionalAccountTransactionFetcher transactionFetcher =
                new SdcTransactionalAccountTransactionFetcher(
                        apiClient, providerMarket, persistentStorage);

        /*
           We are overriding the default date range (by default it is 3 months but we use 1 month)
           See getTransactionsFor method in SdcApiClient class to find out more details
        */

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(transactionFetcher)
                                .setAmountAndUnitToFetch(MONTHS_TO_FETCH, ChronoUnit.MONTHS)
                                .setLocalDateTimeSource(localDateTimeSource)
                                .build()));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }
}
