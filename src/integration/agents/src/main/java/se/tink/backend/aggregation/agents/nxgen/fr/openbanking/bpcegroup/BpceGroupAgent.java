package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup;

import com.google.inject.Inject;
import java.util.Optional;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.apiclient.BpceGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.authenticator.BpceGroupAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.configuration.BpceGroupConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.payment.BpceGroupPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.signature.BpceGroupRequestSigner;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.signature.BpceGroupSignatureHeaderGenerator;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.storage.BpceOAuth2TokenStorage;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.BpceGroupTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.BpceGroupTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.converter.BpceGroupTransactionalAccountConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.FrOpenBankingPaymentExecutor;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.module.QSealcSignerModuleRSASHA256;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

@AgentDependencyModules(modules = QSealcSignerModuleRSASHA256.class)
public final class BpceGroupAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final BpceGroupApiClient bpceGroupApiClient;
    private final BpceGroupPaymentApiClient bpceGroupPaymentApiClient;
    private final BpceOAuth2TokenStorage bpceOAuth2TokenStorage;
    private final StrongAuthenticationState strongAuthenticationState;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    @Inject
    public BpceGroupAgent(AgentComponentProvider componentProvider, QsealcSigner qsealcSigner) {
        super(componentProvider);

        final AgentConfiguration<BpceGroupConfiguration> agentConfiguration =
                getAgentConfiguration();
        final BpceGroupConfiguration bpceGroupConfiguration =
                agentConfiguration.getProviderSpecificConfiguration();
        final BpceGroupSignatureHeaderGenerator bpceGroupSignatureHeaderGenerator =
                createSignatureHeaderGenerator(qsealcSigner, bpceGroupConfiguration);
        final String redirectUrl = agentConfiguration.getRedirectUrl();

        this.bpceOAuth2TokenStorage = new BpceOAuth2TokenStorage(this.persistentStorage);

        this.bpceGroupApiClient =
                new BpceGroupApiClient(
                        this.client,
                        this.bpceOAuth2TokenStorage,
                        bpceGroupConfiguration,
                        redirectUrl,
                        bpceGroupSignatureHeaderGenerator);

        this.bpceGroupPaymentApiClient =
                new BpceGroupPaymentApiClient(
                        client,
                        sessionStorage,
                        agentConfiguration.getProviderSpecificConfiguration(),
                        bpceGroupSignatureHeaderGenerator);

        this.strongAuthenticationState = new StrongAuthenticationState(this.request.getAppUriId());

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
                        new BpceGroupAuthenticator(bpceGroupApiClient, bpceOAuth2TokenStorage),
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

    @Override
    public FetchAccountsResponse fetchSavingsAccounts() {
        return transactionalAccountRefreshController.fetchSavingsAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchSavingsTransactions() {
        return transactionalAccountRefreshController.fetchSavingsTransactions();
    }

    @Override
    public SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    private BpceGroupSignatureHeaderGenerator createSignatureHeaderGenerator(
            QsealcSigner qsealcSigner, BpceGroupConfiguration bpceGroupConfiguration) {

        final BpceGroupRequestSigner bpceGroupRequestSigner =
                new BpceGroupRequestSigner(qsealcSigner);

        return new BpceGroupSignatureHeaderGenerator(
                bpceGroupConfiguration, bpceGroupRequestSigner);
    }

    private AgentConfiguration<BpceGroupConfiguration> getAgentConfiguration() {
        return getAgentConfigurationController()
                .getAgentConfiguration(BpceGroupConfiguration.class);
    }

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        final BpceGroupTransactionalAccountConverter bpceGroupTransactionalAccountConverter =
                new BpceGroupTransactionalAccountConverter();

        final BpceGroupTransactionalAccountFetcher accountFetcher =
                new BpceGroupTransactionalAccountFetcher(
                        bpceGroupApiClient, bpceGroupTransactionalAccountConverter);

        final BpceGroupTransactionFetcher transactionFetcher =
                new BpceGroupTransactionFetcher(bpceGroupApiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionPagePaginationController<>(transactionFetcher, 1)));
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        final AgentConfiguration<BpceGroupConfiguration> agentConfiguration =
                getAgentConfiguration();

        FrOpenBankingPaymentExecutor paymentExecutor =
                new FrOpenBankingPaymentExecutor(
                        bpceGroupPaymentApiClient,
                        agentConfiguration.getRedirectUrl(),
                        sessionStorage,
                        strongAuthenticationState,
                        supplementalInformationHelper);

        return Optional.of(new PaymentController(paymentExecutor, paymentExecutor));
    }
}
