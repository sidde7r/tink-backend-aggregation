package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.INVESTMENTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.LOANS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.MORTGAGE_AGGREGATION;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.PAYMENTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.TRANSFERS;

import com.google.inject.Inject;
import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchEInvoicesResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshEInvoiceExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentPisCapability;
import se.tink.backend.aggregation.agents.contexts.CompositeAgentContext;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.authenticator.HandelsbankenBankIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.authenticator.HandelsbankenSECardDeviceAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.ExecutorExceptionResolver;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.payment.HandelsbankenSEPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.HandelsbankenSEBankTransferExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.creditcard.HandelsbankenSECreditCardAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.creditcard.HandelsbankenSECreditCardTransactionPaginator;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.einvoice.HandelsbankenSEEInvoiceFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.identity.HandelsbankenSEIdentityFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.HandelsbankenSEInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.transactionalaccount.HandelsbankenSEAccountTransactionPaginator;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.transactionalaccount.HandelsbankenSEUpcomingTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.transferdestination.HandelsbankenSETransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.filters.HandelsbankenSEBankSideErrorFilter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.filters.HandelsbankenSEContentTypeFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.HandelsbankenAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.transactionalaccount.HandelsbankenTransactionalAccountFetcher;
import se.tink.backend.aggregation.client.provider_configuration.rpc.PisCapability;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.index.TransactionIndexPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TimeoutFilter;
import se.tink.backend.aggregation.utils.transfer.StringNormalizerSwedish;
import se.tink.backend.aggregation.utils.transfer.TransferMessageFormatter;
import se.tink.backend.aggregation.utils.transfer.TransferMessageLengthConfig;
import se.tink.libraries.i18n.Catalog;

@AgentCapabilities({
    CHECKING_ACCOUNTS,
    LOANS,
    PAYMENTS,
    CREDIT_CARDS,
    SAVINGS_ACCOUNTS,
    IDENTITY_DATA,
    TRANSFERS,
    INVESTMENTS,
    MORTGAGE_AGGREGATION
})
@AgentPisCapability(
        capabilities = {
            PisCapability.PIS_SE_BANK_TRANSFERS,
            PisCapability.PIS_SE_BG,
            PisCapability.PIS_SE_PG,
            PisCapability.PIS_FUTURE_DATE
        },
        markets = {"SE"})
public final class HandelsbankenSEAgent
        extends HandelsbankenAgent<HandelsbankenSEApiClient, HandelsbankenSEConfiguration>
        implements RefreshIdentityDataExecutor,
                RefreshEInvoiceExecutor,
                RefreshInvestmentAccountsExecutor,
                RefreshTransferDestinationExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor {

    private EInvoiceRefreshController eInvoiceRefreshController;
    private final InvestmentRefreshController investmentRefreshController;
    private final TransferDestinationRefreshController transferDestinationRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    @Inject
    public HandelsbankenSEAgent(AgentComponentProvider agentComponentProvider) {
        super(agentComponentProvider, new HandelsbankenSEConfiguration());
        eInvoiceRefreshController = null;

        investmentRefreshController =
                new InvestmentRefreshController(
                        metricRefreshController,
                        updateController,
                        new HandelsbankenSEInvestmentFetcher(
                                bankClient, handelsbankenSessionStorage, credentials));

        transferDestinationRefreshController = constructTransferDestinationRefreshController();

        creditCardRefreshController = constructCreditCardRefreshController();

        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
    }

    @Override
    protected HandelsbankenSEApiClient constructApiClient(
            HandelsbankenSEConfiguration handelsbankenConfiguration) {
        client.addFilter(new TimeoutFilter());
        client.addFilter(new HandelsbankenSEContentTypeFilter());
        client.addFilter(new HandelsbankenSEBankSideErrorFilter());
        return new HandelsbankenSEApiClient(client, handelsbankenConfiguration);
    }

    @Override
    protected TypedAuthenticator[] constructAuthenticators(
            HandelsbankenSEApiClient bankClient,
            HandelsbankenSEConfiguration handelsbankenConfiguration,
            HandelsbankenPersistentStorage handelsbankenPersistentStorage,
            HandelsbankenSessionStorage handelsbankenSessionStorage) {

        HandelsbankenAutoAuthenticator autoAuthenticator = constructAutoAuthenticator();

        return new TypedAuthenticator[] {
            constructAutoAuthenticationController(
                    new HandelsbankenSECardDeviceAuthenticator(
                            bankClient,
                            handelsbankenPersistentStorage,
                            supplementalInformationController,
                            handelsbankenConfiguration,
                            autoAuthenticator),
                    autoAuthenticator),
            new BankIdAuthenticationController<>(
                    supplementalInformationController,
                    new HandelsbankenBankIdAuthenticator(
                            bankClient,
                            credentials,
                            handelsbankenPersistentStorage,
                            handelsbankenSessionStorage),
                    persistentStorage,
                    credentials)
        };
    }

    @Override
    public FetchInvestmentAccountsResponse fetchInvestmentAccounts() {
        return investmentRefreshController.fetchInvestmentAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchInvestmentTransactions() {
        return investmentRefreshController.fetchInvestmentTransactions();
    }

    @Override
    protected Optional<TransferController> constructTransferController(
            HandelsbankenSEApiClient client,
            HandelsbankenSessionStorage sessionStorage,
            CompositeAgentContext context) {

        Catalog catalog = context.getCatalog();
        ExecutorExceptionResolver exceptionResolver = new ExecutorExceptionResolver(catalog);

        HandelsbankenSEPaymentExecutor paymentExecutor =
                new HandelsbankenSEPaymentExecutor(
                        supplementalInformationController,
                        catalog,
                        client,
                        sessionStorage,
                        exceptionResolver);
        HandelsbankenSEBankTransferExecutor transferExecutor =
                new HandelsbankenSEBankTransferExecutor(
                        client,
                        sessionStorage,
                        exceptionResolver,
                        new TransferMessageFormatter(
                                catalog,
                                TransferMessageLengthConfig.createWithMaxLength(14, 12),
                                new StringNormalizerSwedish(",.-?!/+")),
                        catalog,
                        paymentExecutor);

        return Optional.of(new TransferController(paymentExecutor, transferExecutor));
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return transferDestinationRefreshController.fetchTransferDestinations(accounts);
    }

    private TransferDestinationRefreshController constructTransferDestinationRefreshController() {
        return new TransferDestinationRefreshController(
                metricRefreshController,
                new HandelsbankenSETransferDestinationFetcher(
                        bankClient, handelsbankenSessionStorage));
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return creditCardRefreshController.fetchCreditCardTransactions();
    }

    private CreditCardRefreshController constructCreditCardRefreshController() {
        return new CreditCardRefreshController(
                this.metricRefreshController,
                this.updateController,
                new HandelsbankenSECreditCardAccountFetcher(
                        this.bankClient, this.handelsbankenSessionStorage),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(
                                new HandelsbankenSECreditCardTransactionPaginator(
                                        this.bankClient, this.handelsbankenSessionStorage))));
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

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        return new TransactionalAccountRefreshController(
                this.metricRefreshController,
                this.updateController,
                new HandelsbankenTransactionalAccountFetcher(
                        this.bankClient, this.handelsbankenSessionStorage),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionIndexPaginationController<>(
                                new HandelsbankenSEAccountTransactionPaginator(
                                        this.bankClient, this.handelsbankenSessionStorage)),
                        new HandelsbankenSEUpcomingTransactionFetcher(
                                this.bankClient, this.handelsbankenSessionStorage)));
    }

    @Override
    public FetchEInvoicesResponse fetchEInvoices() {
        eInvoiceRefreshController =
                Optional.ofNullable(eInvoiceRefreshController)
                        .orElseGet(
                                () ->
                                        new EInvoiceRefreshController(
                                                metricRefreshController,
                                                new HandelsbankenSEEInvoiceFetcher(
                                                        this.bankClient,
                                                        this.handelsbankenSessionStorage)));
        return new FetchEInvoicesResponse(eInvoiceRefreshController.refreshEInvoices());
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return HandelsbankenSEIdentityFetcher.fetchIdentityData(persistentStorage);
    }
}
