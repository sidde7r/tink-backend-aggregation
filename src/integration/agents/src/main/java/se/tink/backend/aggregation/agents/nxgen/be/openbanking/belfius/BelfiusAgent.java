package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.HttpClient.MAX_RETRIES;
import static se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.HttpClient.RETRY_SLEEP_MILLISECONDS;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.authenticator.BelfiusAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.configuration.BelfiusConfiguration;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount.BelfiusAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount.BelfiusTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.filter.BelfiusClientConfigurator;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.KeyWithInitiDateFromFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionKeyWithInitDateFromFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.OAuth2TokenSessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@Slf4j
@AgentCapabilities({CHECKING_ACCOUNTS})
public final class BelfiusAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor {

    private final BelfiusApiClient apiClient;
    private final AgentConfiguration<BelfiusConfiguration> agentConfiguration;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    @Inject
    public BelfiusAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        this.agentConfiguration =
                getAgentConfigurationController().getAgentConfiguration(BelfiusConfiguration.class);
        this.apiClient =
                new BelfiusApiClient(
                        client, agentConfiguration, componentProvider.getRandomValueGenerator());
        new BelfiusClientConfigurator(componentProvider.getLocalDateTimeSource())
                .configure(
                        client,
                        persistentStorage,
                        MAX_RETRIES,
                        RETRY_SLEEP_MILLISECONDS,
                        credentials.getSessionExpiryDate());
        this.transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        client.setEidasProxy(configuration.getEidasProxy());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new BelfiusAuthenticator(
                                apiClient,
                                persistentStorage,
                                agentConfiguration,
                                credentials.getField(BelfiusConstants.CredentialKeys.IBAN)),
                        credentials,
                        strongAuthenticationState);

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

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        final AccountFetcher<TransactionalAccount> accountFetcher =
                new BelfiusAccountFetcher(apiClient, persistentStorage);

        final KeyWithInitiDateFromFetcher<TransactionalAccount, String> transactionFetcher =
                new BelfiusTransactionFetcher(apiClient, persistentStorage);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionKeyWithInitDateFromFetcherController<>(request, transactionFetcher));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new OAuth2TokenSessionHandler(persistentStorage);
    }
}
