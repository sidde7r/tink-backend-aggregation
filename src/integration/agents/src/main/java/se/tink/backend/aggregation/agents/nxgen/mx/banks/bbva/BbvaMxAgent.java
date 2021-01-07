package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.LOANS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.BbvaMxAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.credit.BbvaMxCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.loan.BbvaMxLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.transactional.BbvaMxTransactionalAccountFetcher;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS, LOANS})
public final class BbvaMxAgent extends NextGenerationAgent
        implements RefreshLoanAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor {

    private final BbvaMxApiClient bbvaApiClient;
    private final LoanRefreshController loanRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public BbvaMxAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        this.bbvaApiClient = new BbvaMxApiClient(client, persistentStorage);

        loanRefreshController =
                new LoanRefreshController(
                        metricRefreshController,
                        updateController,
                        new BbvaMxLoanFetcher(bbvaApiClient));

        creditCardRefreshController = constructCreditCardRefreshController();

        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new BbvaMxAuthenticator(bbvaApiClient, persistentStorage);
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
        BbvaMxTransactionalAccountFetcher transactionalAccountFetcher =
                new BbvaMxTransactionalAccountFetcher(bbvaApiClient, persistentStorage);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                transactionalAccountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(
                                        transactionalAccountFetcher)
                                .build(),
                        transactionalAccountFetcher));
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
        BbvaMxCreditCardFetcher creditCardFetcher =
                new BbvaMxCreditCardFetcher(bbvaApiClient, persistentStorage);

        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                creditCardFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(creditCardFetcher)
                                .build()));
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
    protected SessionHandler constructSessionHandler() {
        return new BbvaMxSessionHandler(bbvaApiClient);
    }
}
