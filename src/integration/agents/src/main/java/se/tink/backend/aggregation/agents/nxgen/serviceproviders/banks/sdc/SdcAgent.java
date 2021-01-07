package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc;

import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConstants.TimeoutFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.converter.AccountNumberToIbanConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.SdcAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.SdcCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.SdcInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.SdcLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.SdcTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.filter.SdcExceptionFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.parser.SdcTransactionParser;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.sessionhandler.SdcSessionHandler;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.TimeoutRetryFilter;
import se.tink.libraries.credentials.service.CredentialsRequest;

public abstract class SdcAgent extends NextGenerationAgent
        implements RefreshInvestmentAccountsExecutor,
                RefreshLoanAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor {

    protected final SdcTransactionParser parser;
    protected SdcConfiguration agentConfiguration;
    protected SdcApiClient bankClient;
    protected SdcSessionStorage sdcSessionStorage;
    protected SdcPersistentStorage sdcPersistentStorage;
    private final InvestmentRefreshController investmentRefreshController;
    private final LoanRefreshController loanRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    protected SdcExceptionFilter sdcExceptionFilter;

    public SdcAgent(
            CredentialsRequest request,
            AgentContext context,
            SignatureKeyPair signatureKeyPair,
            SdcConfiguration agentConfiguration,
            SdcTransactionParser parser) {
        super(request, context, signatureKeyPair);
        configureHttpClient(client);
        this.parser = parser;
        this.agentConfiguration = agentConfiguration;
        this.sdcSessionStorage = new SdcSessionStorage(this.sessionStorage);
        sdcPersistentStorage = new SdcPersistentStorage(this.persistentStorage);
        this.bankClient = this.createApiClient(agentConfiguration);

        this.investmentRefreshController =
                new InvestmentRefreshController(
                        this.metricRefreshController,
                        this.updateController,
                        new SdcInvestmentFetcher(
                                this.bankClient, this.sdcSessionStorage, this.agentConfiguration));

        this.loanRefreshController =
                new LoanRefreshController(
                        this.metricRefreshController,
                        this.updateController,
                        new SdcLoanFetcher(
                                this.bankClient, this.sdcSessionStorage, request.getProvider()));

        this.creditCardRefreshController = constructCreditCardRefreshController();

        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
    }

    protected void configureHttpClient(TinkHttpClient client) {
        sdcExceptionFilter = new SdcExceptionFilter();
        client.setTimeout(SdcConstants.HTTP_TIMEOUT);
        client.addFilter(sdcExceptionFilter);
        client.addFilter(
                new TimeoutRetryFilter(
                        TimeoutFilter.NUM_TIMEOUT_RETRIES,
                        TimeoutFilter.TIMEOUT_RETRY_SLEEP_MILLISECONDS));
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
                new SdcAccountFetcher(this.bankClient, this.sdcSessionStorage, getIbanConverter()),
                new TransactionFetcherController<>(
                        this.transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(
                                        new SdcTransactionFetcher(
                                                this.bankClient,
                                                this.sdcSessionStorage,
                                                this.parser))
                                .build()));
    }

    protected abstract AccountNumberToIbanConverter getIbanConverter();

    @Override
    public FetchLoanAccountsResponse fetchLoanAccounts() {
        return loanRefreshController.fetchLoanAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchLoanTransactions() {
        return loanRefreshController.fetchLoanTransactions();
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new SdcSessionHandler(this.bankClient);
    }

    protected abstract SdcApiClient createApiClient(SdcConfiguration agentConfiguration);

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return creditCardRefreshController.fetchCreditCardTransactions();
    }

    private CreditCardRefreshController constructCreditCardRefreshController() {
        SdcCreditCardFetcher creditCardFetcher =
                new SdcCreditCardFetcher(
                        this.bankClient,
                        this.sdcSessionStorage,
                        this.parser,
                        this.agentConfiguration);

        return new CreditCardRefreshController(
                this.metricRefreshController,
                this.updateController,
                creditCardFetcher,
                new TransactionFetcherController<>(
                        this.transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(creditCardFetcher)
                                .build()));
    }

    @Override
    public FetchInvestmentAccountsResponse fetchInvestmentAccounts() {
        return investmentRefreshController.fetchInvestmentAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchInvestmentTransactions() {
        return investmentRefreshController.fetchInvestmentTransactions();
    }
}
