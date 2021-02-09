package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider;

import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.authenticator.SwedbankDefaultBankIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.authenticator.SwedbankTokenGeneratorAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.configuration.SwedbankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.SwedbankTransferHelper;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.payment.SwedbankDefaultPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.transfer.SwedbankDefaultBankTransferExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.utilities.SwedbankDateUtils;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.creditcard.SwedbankDefaultCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.identitydata.SwedbankIdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.transactional.SwedbankDefaultTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.transferdestination.SwedbankDefaultTransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.interfaces.SwedbankApiClientProvider;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public abstract class SwedbankAbstractAgent extends NextGenerationAgent
        implements RefreshIdentityDataExecutor,
                RefreshTransferDestinationExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor {

    protected final SwedbankConfiguration swedbankConfiguration;
    protected final SwedbankDefaultApiClient apiClient;
    private TransferDestinationRefreshController transferDestinationRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final boolean isBankId;
    private final SwedbankStorage swedbankStorage;
    private final SwedbankDateUtils dateUtils;

    protected SwedbankAbstractAgent(
            AgentComponentProvider componentProvider,
            SwedbankConfiguration configuration,
            SwedbankApiClientProvider apiClientProvider,
            SwedbankDateUtils dateUtils) {
        super(componentProvider);
        this.dateUtils = dateUtils;
        swedbankStorage = new SwedbankStorage();
        this.isBankId =
                request.getProvider().getCredentialsType().equals(CredentialsTypes.MOBILE_BANKID);
        this.swedbankConfiguration = configuration;
        this.apiClient =
                apiClientProvider.createApiClient(
                        client, configuration, swedbankStorage, componentProvider);

        creditCardRefreshController = constructCreditCardRefreshController();

        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
    }

    @Override
    public void setConfiguration(final AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        transferDestinationRefreshController = constructTransferDestinationRefreshController();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new TypedAuthenticationController(constructAuthenticators());
    }

    protected TypedAuthenticator[] constructAuthenticators() {
        return new TypedAuthenticator[] {
            new SwedbankTokenGeneratorAuthenticationController(
                    apiClient, sessionStorage, supplementalInformationHelper),
            new BankIdAuthenticationController<>(
                    supplementalInformationController,
                    new SwedbankDefaultBankIdAuthenticator(
                            apiClient, sessionStorage, credentials.getField(Key.CORPORATE_ID)),
                    persistentStorage,
                    credentials)
        };
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

    protected TransactionalAccountRefreshController
            constructTransactionalAccountRefreshController() {
        SwedbankDefaultTransactionalAccountFetcher transactionalFetcher =
                new SwedbankDefaultTransactionalAccountFetcher(apiClient, persistentStorage);

        TransactionFetcherController<TransactionalAccount> transactionFetcherController =
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(transactionalFetcher),
                        transactionalFetcher);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                transactionalFetcher,
                transactionFetcherController);
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
        SwedbankDefaultCreditCardFetcher creditCardFetcher =
                new SwedbankDefaultCreditCardFetcher(
                        apiClient, request.getProvider().getCurrency());

        TransactionFetcherController<CreditCardAccount> transactionFetcherController =
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(creditCardFetcher));

        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                creditCardFetcher,
                transactionFetcherController);
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return transferDestinationRefreshController.fetchTransferDestinations(accounts);
    }

    private TransferDestinationRefreshController constructTransferDestinationRefreshController() {
        return new TransferDestinationRefreshController(
                metricRefreshController,
                new SwedbankDefaultTransferDestinationFetcher(
                        apiClient, sessionStorage, swedbankStorage));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new SwedbankDefaultSessionHandler(apiClient);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        SwedbankTransferHelper transferHelper =
                new SwedbankTransferHelper(
                        context, catalog, supplementalInformationHelper, apiClient, isBankId);
        SwedbankDefaultBankTransferExecutor transferExecutor =
                new SwedbankDefaultBankTransferExecutor(
                        catalog, apiClient, transferHelper, swedbankStorage, dateUtils);
        SwedbankDefaultPaymentExecutor paymentExecutor =
                new SwedbankDefaultPaymentExecutor(
                        catalog, apiClient, transferHelper, swedbankStorage, dateUtils);
        return Optional.of(new TransferController(paymentExecutor, transferExecutor));
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        final SwedbankIdentityDataFetcher identityDataFetcher =
                new SwedbankIdentityDataFetcher(apiClient);

        return identityDataFetcher.getIdentityDataResponse();
    }
}
