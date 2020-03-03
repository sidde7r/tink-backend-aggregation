package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar;

import java.util.Collections;
import java.util.UUID;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.LansforsakringarConstants.Fetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.LansforsakringarConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.LansforsakringarConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.authenticator.LansforsakringarBankIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.authenticator.rpc.BankIdInitResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.creditcard.CreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.InvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.loan.LoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transactional.TransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transactional.TransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transactional.UpcomingTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.session.LansforsakringarSessionHandler;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.identitydata.countries.SeIdentityData;

public class LansforsakringarAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshIdentityDataExecutor,
                RefreshInvestmentAccountsExecutor,
                RefreshLoanAccountsExecutor {

    private final LansforsakringarApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final InvestmentRefreshController investmentRefreshController;
    private final LoanRefreshController loanRefreshController;

    public LansforsakringarAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        apiClient =
                new LansforsakringarApiClient(client, sessionStorage, catalog, persistentStorage);
        persistentStorage.computeIfAbsent(
                HeaderKeys.DEVICE_ID, k -> UUID.randomUUID().toString().toUpperCase());
        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
        creditCardRefreshController = constructCreditCardRefreshController();
        investmentRefreshController = constructInvestmentRefreshController();
        loanRefreshController = constructLoanRefreshController();

        client.setDebugProxy("http://127.0.0.1:8888");
    }

    private LoanRefreshController constructLoanRefreshController() {
        return new LoanRefreshController(
                metricRefreshController, updateController, new LoanFetcher(apiClient));
    }

    private InvestmentRefreshController constructInvestmentRefreshController() {
        return new InvestmentRefreshController(
                metricRefreshController, updateController, new InvestmentFetcher(apiClient));
    }

    private CreditCardRefreshController constructCreditCardRefreshController() {
        CreditCardFetcher creditCardFetcher = new CreditCardFetcher(apiClient);
        return new CreditCardRefreshController(
                metricRefreshController, updateController, creditCardFetcher, creditCardFetcher);
    }

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new TransactionalAccountFetcher(apiClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionPagePaginationController<>(
                                new TransactionFetcher(apiClient), Fetcher.START_PAGE),
                        new UpcomingTransactionFetcher(apiClient)));
    }

    @Override
    protected Authenticator constructAuthenticator() {
        BankIdAuthenticationController<BankIdInitResponse>
                bankIdResponseBankIdAuthenticationController =
                        new BankIdAuthenticationController(
                                context,
                                new LansforsakringarBankIdAuthenticator(apiClient, sessionStorage),
                                persistentStorage,
                                credentials);
        return new TypedAuthenticationController(bankIdResponseBankIdAuthenticationController);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new LansforsakringarSessionHandler(apiClient);
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
    public FetchIdentityDataResponse fetchIdentityData() {
        return new FetchIdentityDataResponse(
                SeIdentityData.of(
                        sessionStorage.get(StorageKeys.CUSTOMER_NAME),
                        sessionStorage.get(StorageKeys.SSN)));
    }

    @Override
    public FetchInvestmentAccountsResponse fetchInvestmentAccounts() {
        return investmentRefreshController.fetchInvestmentAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchInvestmentTransactions() {
        return new FetchTransactionsResponse(Collections.EMPTY_MAP);
    }

    @Override
    public FetchLoanAccountsResponse fetchLoanAccounts() {
        return loanRefreshController.fetchLoanAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchLoanTransactions() {
        return new FetchTransactionsResponse(Collections.EMPTY_MAP);
    }
}
