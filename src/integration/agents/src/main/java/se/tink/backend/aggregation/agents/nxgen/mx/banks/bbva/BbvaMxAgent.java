package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.BbvaMxAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.credit.BbvaMxCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.loan.BbvaMxLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.transactional.BbvaMxTransactionalAccountFetcher;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class BbvaMxAgent extends NextGenerationAgent implements RefreshLoanAccountsExecutor {

    private final BbvaMxApiClient bbvaApiClient;
    private final LoanRefreshController loanRefreshController;

    public BbvaMxAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        this.bbvaApiClient = new BbvaMxApiClient(client, persistentStorage);

        loanRefreshController =
                new LoanRefreshController(
                        metricRefreshController,
                        updateController,
                        new BbvaMxLoanFetcher(bbvaApiClient));
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new BbvaMxAuthenticator(bbvaApiClient, persistentStorage);
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        BbvaMxTransactionalAccountFetcher transactionalAccountFetcher =
                new BbvaMxTransactionalAccountFetcher(bbvaApiClient, persistentStorage);

        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        transactionalAccountFetcher,
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionDatePaginationController<>(
                                        transactionalAccountFetcher),
                                transactionalAccountFetcher)));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        BbvaMxCreditCardFetcher creditCardFetcher =
                new BbvaMxCreditCardFetcher(bbvaApiClient, persistentStorage);

        return Optional.of(
                new CreditCardRefreshController(
                        metricRefreshController,
                        updateController,
                        creditCardFetcher,
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionDatePaginationController<>(creditCardFetcher))));
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
