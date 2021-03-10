package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.LIST_BENEFICIARIES;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.TRANSFERS;

import com.google.inject.Inject;
import java.util.List;
import java.util.Optional;
import lombok.SneakyThrows;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshBeneficiariesExecutor;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentPisCapability;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.apiclient.BpceGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.authenticator.BpceGroupAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.configuration.BpceGroupConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.creditcard.BpceGroupCardTransactionsFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.creditcard.BpceGroupCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.creditcard.converter.BpceGroupCreditCardConverter;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.fetcher.transfer.BpceGroupTransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.payment.BpceGroupPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.signature.BpceGroupRequestSigner;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.signature.BpceGroupSignatureHeaderGenerator;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.storage.BpceOAuth2TokenStorage;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.BpceGroupTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.BpceGroupTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.converter.BpceGroupTransactionalAccountConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.FrOpenBankingPaymentExecutor;
import se.tink.backend.aggregation.client.provider_configuration.rpc.PisCapability;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.module.QSealcSignerModuleRSASHA256;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

@AgentDependencyModules(modules = QSealcSignerModuleRSASHA256.class)
@AgentCapabilities({CHECKING_ACCOUNTS, CREDIT_CARDS, TRANSFERS, LIST_BENEFICIARIES})
@AgentPisCapability(capabilities = PisCapability.PIS_SEPA)
public final class BpceGroupAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshTransferDestinationExecutor,
                RefreshBeneficiariesExecutor,
                RefreshCreditCardAccountsExecutor {

    private final BpceGroupApiClient bpceGroupApiClient;
    private final BpceGroupPaymentApiClient bpceGroupPaymentApiClient;
    private final BpceOAuth2TokenStorage bpceOAuth2TokenStorage;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final TransferDestinationRefreshController transferDestinationRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;

    @Inject
    @SneakyThrows
    public BpceGroupAgent(AgentComponentProvider componentProvider, QsealcSigner qsealcSigner) {
        super(componentProvider);

        final AgentConfiguration<BpceGroupConfiguration> agentConfiguration =
                getAgentConfiguration();
        final BpceGroupConfiguration bpceGroupConfiguration =
                agentConfiguration.getProviderSpecificConfiguration();
        bpceGroupConfiguration.setClientId(
                CertificateUtils.getOrganizationIdentifier(agentConfiguration.getQsealc()));
        bpceGroupConfiguration.setServerUrl(
                componentProvider.getCredentialsRequest().getProvider().getPayload());
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

        this.transactionalAccountRefreshController = getTransactionalAccountRefreshController();

        this.transferDestinationRefreshController = constructTransferDestinationRefreshController();

        this.creditCardRefreshController = getCreditCardRefreshController();
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
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return creditCardRefreshController.fetchCreditCardTransactions();
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

    private CreditCardRefreshController getCreditCardRefreshController() {
        final BpceGroupCreditCardConverter converter = new BpceGroupCreditCardConverter();
        final BpceGroupCreditCardFetcher creditCardFetcher =
                new BpceGroupCreditCardFetcher(bpceGroupApiClient, converter);
        final BpceGroupCardTransactionsFetcher transactionsFetcher =
                new BpceGroupCardTransactionsFetcher(bpceGroupApiClient);

        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                creditCardFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionPagePaginationController<>(transactionsFetcher, 1)));
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

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return transferDestinationRefreshController.fetchTransferDestinations(accounts);
    }

    private TransferDestinationRefreshController constructTransferDestinationRefreshController() {
        return new TransferDestinationRefreshController(
                metricRefreshController,
                new BpceGroupTransferDestinationFetcher(bpceGroupApiClient));
    }

    @Override
    public FetchTransferDestinationsResponse fetchBeneficiaries(List<Account> accounts) {
        return transferDestinationRefreshController.fetchTransferDestinations(accounts);
    }
}
