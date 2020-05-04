package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.aktia;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.aktia.apiclient.AktiaApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.aktia.authenticator.AktiaAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.aktia.authenticator.AktiaOtpDataStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.aktia.authenticator.steps.helpers.AktiaAccessTokenRetriever;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.aktia.authenticator.steps.helpers.AktiaLoginDetailsFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.aktia.authenticator.steps.helpers.AktiaOtpCodeExchanger;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.aktia.configuration.AktiaConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.aktia.transactionalaccount.AktiaTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.aktia.transactionalaccount.AktiaTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.aktia.transactionalaccount.converter.AktiaTransactionalAccountConverter;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.SubsequentProgressiveGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2TokenStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;

public class AktiaAgent extends SubsequentProgressiveGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final AktiaApiClient aktiaApiClient;
    private final OAuth2TokenStorage tokenStorage;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    @Inject
    public AktiaAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);

        final AktiaConfiguration aktiaConfiguration = getAgentConfiguration();

        this.tokenStorage = new OAuth2TokenStorage(this.persistentStorage, this.sessionStorage);
        this.aktiaApiClient =
                new AktiaApiClient(this.client, aktiaConfiguration, this.tokenStorage);
        this.transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    @Override
    public void setConfiguration(final AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        client.setEidasProxy(configuration.getEidasProxy());
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

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public StatelessProgressiveAuthenticator getAuthenticator() {
        final SupplementalInformationFormer supplementalInformationFormer =
                new SupplementalInformationFormer(request.getProvider());
        final AktiaAccessTokenRetriever accessTokenRetriever =
                new AktiaAccessTokenRetriever(aktiaApiClient, tokenStorage);
        final AktiaOtpDataStorage otpDataStorage = new AktiaOtpDataStorage(sessionStorage);
        final AktiaLoginDetailsFetcher loginDetailsFetcher =
                new AktiaLoginDetailsFetcher(aktiaApiClient, otpDataStorage);
        final AktiaOtpCodeExchanger otpCodeExchanger =
                new AktiaOtpCodeExchanger(aktiaApiClient, otpDataStorage);

        return new AktiaAuthenticator(
                supplementalInformationFormer,
                accessTokenRetriever,
                loginDetailsFetcher,
                otpCodeExchanger);
    }

    private AktiaConfiguration getAgentConfiguration() {
        return getAgentConfigurationController().getAgentConfiguration(AktiaConfiguration.class);
    }

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        final AktiaTransactionalAccountConverter transactionalAccountConverter =
                new AktiaTransactionalAccountConverter();

        final AktiaTransactionalAccountFetcher accountFetcher =
                new AktiaTransactionalAccountFetcher(aktiaApiClient, transactionalAccountConverter);

        final AktiaTransactionFetcher transactionFetcher =
                new AktiaTransactionFetcher(aktiaApiClient, transactionalAccountConverter);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(transactionFetcher)));
    }
}
