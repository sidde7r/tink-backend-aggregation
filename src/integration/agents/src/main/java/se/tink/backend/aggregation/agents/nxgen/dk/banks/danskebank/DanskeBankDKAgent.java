package se.tink.backend.aggregation.agents.nxgen.dk.banks.danskebank;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.DanskeBankChallengeAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.DanskeBankCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.DanskeBankLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.DanskeBankMultiTransactionsFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.DanskeBankTransactionalAccountFetcher;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class DanskeBankDKAgent extends DanskeBankAgent
        implements RefreshLoanAccountsExecutor, RefreshCreditCardAccountsExecutor {

    private static final int DK_MAX_CONSECUTIVE_EMPTY_PAGES = 8;
    private final LoanRefreshController loanRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;

    public DanskeBankDKAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, new DanskeBankDKConfiguration());

        // DK fetches loans at a separate loan endpoint
        this.loanRefreshController =
                new LoanRefreshController(
                        this.metricRefreshController,
                        this.updateController,
                        new DanskeBankLoanFetcher(
                                this.credentials, this.apiClient, this.configuration));

        this.creditCardRefreshController = constructCreditCardRefreshController();

        configureHttpClient(client);
    }

    @Override
    protected DanskeBankApiClient createApiClient(
            TinkHttpClient client, DanskeBankConfiguration configuration) {
        return new DanskeBankDKApiClient(client, (DanskeBankDKConfiguration) configuration);
    }

    protected void configureHttpClient(TinkHttpClient client) {
        client.disableSignatureRequestHeader();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        DanskeBankChallengeAuthenticator danskeBankChallengeAuthenticator =
                new DanskeBankChallengeAuthenticator(
                        catalog,
                        supplementalInformationHelper,
                        apiClient,
                        persistentStorage,
                        credentials,
                        deviceId,
                        configuration);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                danskeBankChallengeAuthenticator,
                danskeBankChallengeAuthenticator);
    }

    @Override
    public FetchLoanAccountsResponse fetchLoanAccounts() {
        return loanRefreshController.fetchLoanAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchLoanTransactions() {
        return loanRefreshController.fetchLoanTransactions();
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        return Optional.of(
                new TransactionalAccountRefreshController(
                        this.metricRefreshController,
                        this.updateController,
                        new DanskeBankTransactionalAccountFetcher(
                                this.credentials, this.apiClient, this.configuration),
                        createTransactionFetcherController()));
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
                new DanskeBankCreditCardFetcher(
                        this.apiClient, this.configuration.getLanguageCode()),
                createTransactionFetcherController());
    }

    private TransactionFetcherController createTransactionFetcherController() {
        DanskeBankMultiTransactionsFetcher<? extends Account> transactionFetcher =
                new DanskeBankMultiTransactionsFetcher<>(
                        this.apiClient, this.configuration.getLanguageCode());
        return new TransactionFetcherController<>(
                this.transactionPaginationHelper,
                new TransactionDatePaginationController<>(
                        transactionFetcher, DK_MAX_CONSECUTIVE_EMPTY_PAGES),
                transactionFetcher);
    }
}
