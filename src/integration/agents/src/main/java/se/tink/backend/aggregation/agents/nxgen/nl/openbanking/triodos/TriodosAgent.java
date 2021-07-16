package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.authenticator.ConsentStatusFetcher;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.authenticator.TriodosAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.configuration.TriodosConfiguration;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.fetcher.TriodosTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.BerlinGroupAccountFetcher;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.module.QSealcSignerModuleRSASHA256;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationFlow;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TimeoutFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.ConnectionTimeoutRetryFilter;

@AgentDependencyModules(modules = QSealcSignerModuleRSASHA256.class)
@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS})
public final class TriodosAgent extends BerlinGroupAgent<TriodosApiClient, TriodosConfiguration> {

    private final QsealcSigner qsealcSigner;
    private final ConsentStatusFetcher consentStatusFetcher;

    @Inject
    public TriodosAgent(AgentComponentProvider componentProvider, QsealcSigner qsealcSigner) {
        super(componentProvider);

        configureHttpClient();

        this.qsealcSigner = qsealcSigner;
        this.apiClient = createApiClient();
        this.transactionalAccountRefreshController = getTransactionalAccountRefreshController();
        this.consentStatusFetcher = createConsentStatusFetcher();
    }

    private void configureHttpClient() {
        client.addFilter(new TimeoutFilter());
        client.addFilter(
                new ConnectionTimeoutRetryFilter(
                        TriodosConstants.HttpClient.MAX_RETRIES,
                        TriodosConstants.HttpClient.RETRY_SLEEP_MILLISECONDS));
    }

    @Override
    protected TriodosApiClient createApiClient() {
        return new TriodosApiClient(
                client,
                persistentStorage,
                getConfiguration().getProviderSpecificConfiguration(),
                request,
                getConfiguration().getRedirectUrl(),
                qsealcSigner,
                getConfiguration().getQsealc());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return OAuth2AuthenticationFlow.create(
                request,
                systemUpdater,
                persistentStorage,
                supplementalInformationHelper,
                new TriodosAuthenticator(apiClient, persistentStorage, consentStatusFetcher),
                credentials,
                strongAuthenticationState);
    }

    @Override
    protected Class<TriodosConfiguration> getConfigurationClassDescription() {
        return TriodosConfiguration.class;
    }

    protected ConsentStatusFetcher createConsentStatusFetcher() {
        return new ConsentStatusFetcher(persistentStorage, apiClient);
    }

    @Override
    public TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        BerlinGroupAccountFetcher transactionalAccountFetcher =
                new BerlinGroupAccountFetcher(apiClient);

        TriodosTransactionFetcher transationalTransactionFetcher =
                new TriodosTransactionFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                transactionalAccountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(transationalTransactionFetcher)));
    }
}
