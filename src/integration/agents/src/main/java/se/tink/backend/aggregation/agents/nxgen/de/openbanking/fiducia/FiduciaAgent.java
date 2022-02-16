package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.SAVINGS_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.TRANSFERS;

import com.google.inject.Inject;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentPisCapability;
import se.tink.backend.aggregation.agents.agentcapabilities.PisCapability;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.FiduciaAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.FiduciaEmbeddedFieldBuilder;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.FiduciaIconUrlMapper;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.FiduciaPaymentsEmbeddedFieldBuilder;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.fetcher.transactionalaccount.FiduciaTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.FiduciaPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.FiduciaPaymentMapper;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.FiduciaPaymentStatusMapper;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.BasePaymentExecutor;
import se.tink.backend.aggregation.agents.utils.transfer.InferredTransferDestinations;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.module.QSealcSignerModuleRSASHA256;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.payment.exception.PaymentControllerExceptionMapper;
import se.tink.backend.aggregation.nxgen.controllers.payment.validation.impl.SepaCapabilitiesInitializationValidator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TimeoutFilter;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.enums.MarketCode;

@AgentDependencyModules(modules = QSealcSignerModuleRSASHA256.class)
@Slf4j
@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, TRANSFERS})
@AgentPisCapability(
        capabilities = {
            PisCapability.SEPA_CREDIT_TRANSFER,
            PisCapability.SEPA_INSTANT_CREDIT_TRANSFER,
            PisCapability.PIS_SEPA_RECURRING_PAYMENTS
        })
public final class FiduciaAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshTransferDestinationExecutor {

    private final FiduciaApiClient apiClient;
    private final FiduciaPaymentApiClient paymentApiClient;
    private final FiduciaAuthenticator fiduciaAuthenticator;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    @Inject
    public FiduciaAgent(AgentComponentProvider componentProvider, QsealcSigner qsealcSigner) {
        super(componentProvider);
        FiduciaProviderInfo providerInfo =
                parsePayloadToProviderInfo(componentProvider.getProvider().getPayload());

        String userIp = componentProvider.getUser().getIpAddress();

        RandomValueGenerator randomValueGenerator = componentProvider.getRandomValueGenerator();

        this.apiClient =
                new FiduciaApiClient(
                        client,
                        persistentStorage,
                        userIp,
                        providerInfo.getServerUrl(),
                        randomValueGenerator);
        this.paymentApiClient =
                new FiduciaPaymentApiClient(
                        apiClient,
                        credentials,
                        new FiduciaPaymentMapper(
                                randomValueGenerator,
                                componentProvider.getLocalDateTimeSource(),
                                providerInfo.getBic()),
                        randomValueGenerator);
        fiduciaAuthenticator =
                new FiduciaAuthenticator(
                        credentials,
                        apiClient,
                        persistentStorage,
                        supplementalInformationController,
                        new FiduciaEmbeddedFieldBuilder(catalog, new FiduciaIconUrlMapper()));
        this.transactionalAccountRefreshController = getTransactionalAccountRefreshController();
        client.addFilter(
                new FiduciaSigningFilter(
                        qsealcSigner,
                        getAgentConfigurationController()
                                .getAgentConfiguration(FiduciaConfiguration.class)));
        client.addFilter(new TimeoutFilter());
    }

    private FiduciaProviderInfo parsePayloadToProviderInfo(String payload) {
        String[] parts = payload.split(" ");
        FiduciaProviderInfo.FiduciaProviderInfoBuilder infoBuilder =
                FiduciaProviderInfo.builder().serverUrl(parts[0]);

        if (parts.length > 1) {
            infoBuilder.bic(parts[1]);
        }

        return infoBuilder.build();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new AutoAuthenticationController(
                request, context, fiduciaAuthenticator, fiduciaAuthenticator);
    }

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        final FiduciaTransactionalAccountFetcher accountFetcher =
                new FiduciaTransactionalAccountFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(accountFetcher)));
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
    public Optional<PaymentController> constructPaymentController() {
        final BasePaymentExecutor paymentExecutor =
                new BasePaymentExecutor(
                        paymentApiClient,
                        new FiduciaAuthenticator(
                                credentials,
                                apiClient,
                                persistentStorage,
                                supplementalInformationController,
                                new FiduciaPaymentsEmbeddedFieldBuilder(
                                        catalog, new FiduciaIconUrlMapper())),
                        sessionStorage,
                        new FiduciaPaymentStatusMapper());

        return Optional.of(
                PaymentController.builder()
                        .paymentExecutor(paymentExecutor)
                        .fetchablePaymentExecutor(paymentExecutor)
                        .exceptionHandler(new PaymentControllerExceptionMapper())
                        .validator(
                                new SepaCapabilitiesInitializationValidator(
                                        this.getClass(), MarketCode.valueOf(provider.getMarket())))
                        .build());
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return InferredTransferDestinations.forPaymentAccounts(
                accounts, AccountIdentifierType.IBAN);
    }
}
