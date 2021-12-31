package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.PAYMENTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.SAVINGS_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.TRANSFERS;

import com.google.inject.Inject;
import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentPisCapability;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.authenticator.DnbAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.DnbPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.card.DnbCardAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.card.DnbCardTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.mapper.DnbAccountMapper;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.mapper.DnbCardMapper;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.mapper.DnbTransactionMapper;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.transactional.DnbAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.transactional.DnbTransactionFetcher;
import se.tink.backend.aggregation.agents.utils.transfer.InferredTransferDestinations;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.credentials.service.UserAvailability;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS, TRANSFERS, PAYMENTS})
@AgentPisCapability
public final class DnbAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshTransferDestinationExecutor {

    private final DnbStorage storage;
    private final DnbApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalRefreshController;
    private final CreditCardRefreshController cardRefreshController;

    @Inject
    public DnbAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        storage = new DnbStorage(persistentStorage);
        UserAvailability userAvailability =
                componentProvider.getCredentialsRequest().getUserAvailability();
        apiClient =
                new DnbApiClient(
                        client,
                        setupHeaderValues(userAvailability),
                        componentProvider.getRandomValueGenerator(),
                        componentProvider.getLocalDateTimeSource());
        transactionalRefreshController = constructTransactionalRefreshController(componentProvider);
        cardRefreshController =
                constructCardAccountRefreshController(componentProvider, userAvailability);
    }

    private DnbHeaderValues setupHeaderValues(UserAvailability userAvailability) {
        String psuId = credentials.getField(DnbConstants.CredentialsKeys.PSU_ID);
        String redirectUrl =
                getAgentConfigurationController()
                        .getAgentConfiguration(DnbConfiguration.class)
                        .getRedirectUrl();
        String userIpHeaderValue =
                userAvailability.isUserPresent() ? userAvailability.getOriginatingUserIp() : null;
        return new DnbHeaderValues(psuId, redirectUrl, userIpHeaderValue);
    }

    private TransactionalAccountRefreshController constructTransactionalRefreshController(
            AgentComponentProvider componentProvider) {
        UserAvailability userAvailability =
                componentProvider.getCredentialsRequest().getUserAvailability();
        LocalDateTimeSource localDateTimeSource = componentProvider.getLocalDateTimeSource();
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new DnbAccountFetcher(storage, apiClient, new DnbAccountMapper()),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(
                                new DnbTransactionFetcher(
                                        storage,
                                        apiClient,
                                        new DnbTransactionMapper(),
                                        userAvailability,
                                        localDateTimeSource))));
    }

    private CreditCardRefreshController constructCardAccountRefreshController(
            AgentComponentProvider componentProvider, UserAvailability userAvailability) {
        DnbCardTransactionFetcher cardTransactionFetcher =
                new DnbCardTransactionFetcher(
                        storage, apiClient, new DnbTransactionMapper(), userAvailability);

        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                new DnbCardAccountFetcher(storage, apiClient, new DnbCardMapper()),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(cardTransactionFetcher)
                                .setLocalDateTimeSource(componentProvider.getLocalDateTimeSource())
                                .setAmountAndUnitToFetch(
                                        DnbConstants.CreditCardFetcherValues.AT_A_TIME,
                                        DnbConstants.CreditCardFetcherValues.TIME_UNIT)
                                .build()));
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        client.setEidasProxy(configuration.getEidasProxy());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final DnbAuthenticator dnbAuthenticator =
                new DnbAuthenticator(
                        supplementalInformationHelper,
                        strongAuthenticationState,
                        storage,
                        apiClient,
                        credentials);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        dnbAuthenticator, supplementalInformationHelper),
                dnbAuthenticator);
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return cardRefreshController.fetchCreditCardTransactions();
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return transactionalRefreshController.fetchCheckingTransactions();
    }

    @Override
    public FetchAccountsResponse fetchSavingsAccounts() {
        return transactionalRefreshController.fetchSavingsAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchSavingsTransactions() {
        return transactionalRefreshController.fetchSavingsTransactions();
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return transactionalRefreshController.fetchCheckingAccounts();
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return cardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        DnbPaymentExecutor dnbPaymentExecutor =
                new DnbPaymentExecutor(
                        apiClient,
                        sessionStorage,
                        strongAuthenticationState,
                        supplementalInformationHelper);

        return Optional.of(new PaymentController(dnbPaymentExecutor));
    }

    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return InferredTransferDestinations.forPaymentAccounts(
                accounts,
                AccountIdentifierType.IBAN,
                AccountIdentifierType.NO,
                AccountIdentifierType.BBAN);
    }
}
