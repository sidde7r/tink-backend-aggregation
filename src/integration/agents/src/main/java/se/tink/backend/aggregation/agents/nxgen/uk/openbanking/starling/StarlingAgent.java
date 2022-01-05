package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.TRANSFERS;
import static se.tink.backend.aggregation.agents.agentcapabilities.PisCapability.FASTER_PAYMENTS;

import com.google.inject.Inject;
import java.time.ZoneId;
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
import se.tink.backend.aggregation.agents.agentplatform.AgentPlatformHttpClient;
import se.tink.backend.aggregation.agents.agentplatform.authentication.AgentPlatformAgent;
import se.tink.backend.aggregation.agents.agentplatform.authentication.AgentPlatformAuthenticator;
import se.tink.backend.aggregation.agents.agentplatform.authentication.storage.AgentPlatformStorageMigration;
import se.tink.backend.aggregation.agents.agentplatform.authentication.storage.AgentPlatformStorageMigrator;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.auth.StarlingOAuth2AuthenticationConfig;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.auth.StarlingOAuth2AuthorizationSpecification;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.payment.StarlingPaymentAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.payment.StarlingPaymentAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.payment.StarlingPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.payment.auth.PaymentMessageSigner;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.StarlingTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.StarlingTransactionPaginationController;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.StarlingTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.filter.StarlingTerminatedHandshakeRetryFilter;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.secrets.StarlingSecrets;
import se.tink.backend.aggregation.agents.utils.transfer.InferredTransferDestinations;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcess;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.eidasidentity.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController.Builder;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.filter.filters.RateLimitFilter;
import se.tink.libraries.account.enums.AccountIdentifierType;

@AgentCapabilities({CHECKING_ACCOUNTS, TRANSFERS})
@AgentPisCapability(capabilities = FASTER_PAYMENTS, markets = "GB")
@Slf4j
public final class StarlingAgent extends AgentPlatformAgent
        implements RefreshTransferDestinationExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                AgentPlatformAuthenticator,
                AgentPlatformStorageMigration {
    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("CET");
    private final AgentComponentProvider componentProvider;
    private final AgentConfiguration<StarlingSecrets> agentConfiguration;
    private final StarlingApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    @Inject
    public StarlingAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        this.componentProvider = componentProvider;
        this.agentConfiguration =
                getAgentConfigurationController().getAgentConfiguration(StarlingSecrets.class);
        this.apiClient = new StarlingApiClient(client, persistentStorage);
        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController(
                        componentProvider.getLocalDateTimeSource());
        client.addFilter(new RateLimitFilter(provider.getName(), 500, 1500, 3));
        client.addFilter(new StarlingTerminatedHandshakeRetryFilter(3, 1500));
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
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return InferredTransferDestinations.forPaymentAccounts(
                accounts, AccountIdentifierType.SORT_CODE);
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        StarlingPaymentAuthenticator starlingPaymentAuthenticator =
                new StarlingPaymentAuthenticator(
                        agentConfiguration, new AgentPlatformHttpClient(client));
        StarlingPaymentAuthenticationController starlingPaymentAuthenticationController =
                new StarlingPaymentAuthenticationController(
                        supplementalInformationHelper,
                        strongAuthenticationState,
                        starlingPaymentAuthenticator);
        PaymentMessageSigner paymentMessageSigner = new PaymentMessageSigner(agentConfiguration);
        StarlingPaymentExecutor starlingPaymentExecutor =
                new StarlingPaymentExecutor(
                        apiClient, starlingPaymentAuthenticationController, paymentMessageSigner);
        return Optional.of(new PaymentController(starlingPaymentExecutor, starlingPaymentExecutor));
    }

    public AgentAuthenticationProcess getAuthenticationProcess() {
        return new StarlingOAuth2AuthenticationConfig()
                .authenticationProcess(
                        new AgentPlatformHttpClient(client),
                        new StarlingOAuth2AuthorizationSpecification(
                                agentConfiguration, componentProvider));
    }

    public boolean isBackgroundRefreshPossible() {
        return true;
    }

    @Override
    public AgentPlatformStorageMigrator getMigrator() {
        return new StarlingAgentPlatformStorageMigrator();
    }

    @Override
    public void setConfiguration(final AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        client.setEidasProxy(configuration.getEidasProxy());
        client.setEidasIdentity(
                new EidasIdentity(
                        context.getClusterId(),
                        context.getAppId(),
                        context.getCertId(),
                        context.getProviderId(),
                        getAgentClass()));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController(
            LocalDateTimeSource localDateTimeSource) {
        StarlingTransactionFetcher starlingTransactionFetcher =
                new StarlingTransactionFetcher(apiClient);
        TransactionDatePaginationController<TransactionalAccount>
                defaultTransactionPaginationController =
                        new Builder<>(starlingTransactionFetcher)
                                .setConsecutiveEmptyPagesLimit(8)
                                .setLocalDateTimeSource(localDateTimeSource)
                                .setZoneId(DEFAULT_ZONE_ID)
                                .build();

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new StarlingTransactionalAccountFetcher(apiClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new StarlingTransactionPaginationController<>(
                                defaultTransactionPaginationController,
                                starlingTransactionFetcher,
                                localDateTimeSource,
                                DEFAULT_ZONE_ID)));
    }
}
