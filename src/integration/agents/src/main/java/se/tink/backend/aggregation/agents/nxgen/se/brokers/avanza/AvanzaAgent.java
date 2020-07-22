package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza;

import com.google.inject.Inject;
import java.time.temporal.ChronoUnit;
import se.tink.backend.agents.rpc.Field;
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
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.authenticator.AvanzaBankIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.AvanzaInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.loan.AvanzaLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.transactionalaccount.AvanzaTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.session.AvanzaSessionHandler;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.BankServiceInternalErrorFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.ServiceUnavailableBankServiceErrorFilter;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;
import se.tink.libraries.identitydata.countries.SeIdentityData;

public final class AvanzaAgent extends NextGenerationAgent
        implements RefreshIdentityDataExecutor,
                RefreshInvestmentAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshLoanAccountsExecutor {

    private final AvanzaAuthSessionStorage authSessionStorage;
    private final AvanzaApiClient apiClient;
    private final TemporaryStorage temporaryStorage;
    private final InvestmentRefreshController investmentRefreshController;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final LoanRefreshController loanRefreshController;
    private final LocalDateTimeSource localDateTimeSource;

    @Inject
    public AvanzaAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);

        configureHttpClient(client);

        this.authSessionStorage = new AvanzaAuthSessionStorage();
        this.apiClient = new AvanzaApiClient(client, authSessionStorage);
        this.temporaryStorage = new TemporaryStorage();
        this.localDateTimeSource = componentProvider.getLocalDateTimeSource();
        this.investmentRefreshController = constructInvestmentRefreshController();

        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();

        this.loanRefreshController = constructLoanRefreshController();
    }

    private void configureHttpClient(final TinkHttpClient client) {
        client.addFilter(new BankServiceInternalErrorFilter());
        client.addFilter(new ServiceUnavailableBankServiceErrorFilter());
    }

    private LoanRefreshController constructLoanRefreshController() {
        return new LoanRefreshController(
                metricRefreshController,
                updateController,
                new AvanzaLoanFetcher(apiClient, authSessionStorage, temporaryStorage));
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new BankIdAuthenticationController<>(
                supplementalRequester,
                new AvanzaBankIdAuthenticator(
                        apiClient, authSessionStorage, temporaryStorage, sessionStorage),
                persistentStorage,
                credentials);
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
        final AvanzaTransactionalAccountFetcher accountFetcher =
                new AvanzaTransactionalAccountFetcher(
                        apiClient, authSessionStorage, temporaryStorage);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(
                                accountFetcher, 3, 3, ChronoUnit.MONTHS, localDateTimeSource)));
    }

    private InvestmentRefreshController constructInvestmentRefreshController() {
        final AvanzaInvestmentFetcher investmentFetcher =
                new AvanzaInvestmentFetcher(
                        apiClient, authSessionStorage, temporaryStorage, localDateTimeSource);

        return new InvestmentRefreshController(
                metricRefreshController,
                updateController,
                investmentFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(
                                investmentFetcher, 3, 3, ChronoUnit.MONTHS, localDateTimeSource)));
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
    protected SessionHandler constructSessionHandler() {
        AvanzaSessionHandler avanzaSessionHandler =
                new AvanzaSessionHandler(apiClient, authSessionStorage);
        return avanzaSessionHandler;
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return new FetchIdentityDataResponse(
                SeIdentityData.of("", credentials.getField(Field.Key.USERNAME)));
    }

    @Override
    public FetchLoanAccountsResponse fetchLoanAccounts() {
        return loanRefreshController.fetchLoanAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchLoanTransactions() {
        return loanRefreshController.fetchLoanTransactions();
    }
}
