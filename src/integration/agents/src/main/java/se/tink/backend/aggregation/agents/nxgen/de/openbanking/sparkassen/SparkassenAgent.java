package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.TRANSFERS;

import com.google.inject.Inject;
import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentPisCapability;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.SparkassenAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.SparkassenPaymentAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.SparkassenAccountsFetcher;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.SparkassenTransactionsFetcher;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.filter.RequestNotProcessedFilter;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.BasePaymentExecutor;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.BasePaymentMapper;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentAuthenticator;
import se.tink.backend.aggregation.agents.utils.transfer.InferredTransferDestinations;
import se.tink.backend.aggregation.client.provider_configuration.rpc.PisCapability;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.http.filter.filters.AccessExceededFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.BankServiceDownExceptionFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TerminatedHandshakeRetryFilter;
import se.tink.libraries.account.enums.AccountIdentifierType;

@AgentCapabilities({CHECKING_ACCOUNTS, TRANSFERS})
@AgentPisCapability(
        capabilities = {
            PisCapability.SEPA_CREDIT_TRANSFER,
            PisCapability.SEPA_INSTANT_CREDIT_TRANSFER,
            PisCapability.PIS_SEPA_RECURRING_PAYMENTS
        })
public class SparkassenAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshTransferDestinationExecutor {

    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    protected final SparkassenApiClient apiClient;
    protected final SparkassenStorage sparkassenStorage;
    protected RandomValueGenerator randomValueGenerator;
    protected LocalDateTimeSource localDateTimeSource;

    @Inject
    public SparkassenAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        randomValueGenerator = componentProvider.getRandomValueGenerator();
        localDateTimeSource = componentProvider.getLocalDateTimeSource();
        sparkassenStorage = new SparkassenStorage(persistentStorage);

        apiClient = constructApiClient();
        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        client.setEidasProxy(configuration.getEidasProxy());
        client.addFilter(new BankServiceDownExceptionFilter());
        client.addFilter(new AccessExceededFilter());
        client.addFilter(new RequestNotProcessedFilter());
        client.addFilter(new TerminatedHandshakeRetryFilter());
    }

    protected SparkassenApiClient constructApiClient() {
        String bankCode = provider.getPayload();
        SparkassenHeaderValues headerValues =
                new SparkassenHeaderValues(
                        bankCode, request.getUserAvailability().getOriginatingUserIpOrDefault());
        return new SparkassenApiClient(
                client,
                headerValues,
                sparkassenStorage,
                randomValueGenerator,
                localDateTimeSource,
                new BasePaymentMapper());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        SparkassenAuthenticator sparkassenAuthenticator =
                new SparkassenAuthenticator(
                        apiClient,
                        supplementalInformationController,
                        sparkassenStorage,
                        credentials,
                        catalog);

        return new AutoAuthenticationController(
                request, context, sparkassenAuthenticator, sparkassenAuthenticator);
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
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new SparkassenAccountsFetcher(apiClient, sparkassenStorage),
                new SparkassenTransactionsFetcher(apiClient, sparkassenStorage));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        PaymentAuthenticator sparkassenPaymentAuthenticator =
                new SparkassenPaymentAuthenticator(
                        apiClient,
                        supplementalInformationController,
                        sparkassenStorage,
                        credentials,
                        catalog);
        BasePaymentExecutor paymentExecutor =
                new BasePaymentExecutor(apiClient, sparkassenPaymentAuthenticator, sessionStorage);

        return Optional.of(new PaymentController(paymentExecutor, paymentExecutor));
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return InferredTransferDestinations.forPaymentAccounts(
                accounts, AccountIdentifierType.IBAN);
    }
}
