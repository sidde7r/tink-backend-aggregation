package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.authenticator.AsLhvPasswordAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.fetcher.transactionalaccount.AsLhvTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.fetcher.transactionalaccount.AsLhvTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.session.AsLhvSessionHandler;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.common.config.SignatureKeyPair;

public class AsLhvAgent extends NextGenerationAgent {

    private final AsLhvApiClient apiClient;
    private final AsLhvSessionStorage asLhvSessionStorage;

    public AsLhvAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        asLhvSessionStorage = new AsLhvSessionStorage(sessionStorage);
        this.apiClient = new AsLhvApiClient(this.client, asLhvSessionStorage);
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
        client.setDebugOutput(true);
        client.setProxy("http://127.0.0.1:8888");
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(new AsLhvPasswordAuthenticator(apiClient));
    }

    @Override
    protected Optional<TransactionalAccountRefreshController> constructTransactionalAccountRefreshController() {
        final AsLhvTransactionFetcher transactionFetcher = new AsLhvTransactionFetcher(apiClient);
        return Optional.of(new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new AsLhvTransactionalAccountFetcher(apiClient),
                new TransactionFetcherController<>(
                        this.transactionPaginationHelper,
                        new TransactionDatePaginationController<>(transactionFetcher)
                )
        ));
        //return Optional.empty();
//        final BawagPskTransactionFetcher transactionFetcher = new BawagPskTransactionFetcher(apiClient);
//        return Optional.of(new TransactionalAccountRefreshController(
//                metricRefreshController,
//                updateController,
//                new BawagPskTransactionalAccountFetcher(apiClient),
//                new TransactionFetcherController<>(
//                        this.transactionPaginationHelper,
//                        // TODO Alternatively, implement a custom pagination controller which keeps fetching
//                        // transactions until AccountStatementItem/Position equals 1 (signifying the earliest entry)
//                        new TransactionDatePaginationController<>(transactionFetcher)
//                )));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        return Optional.empty();
    }

    @Override
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController() {
        return Optional.empty();
    }

    @Override
    protected Optional<LoanRefreshController> constructLoanRefreshController() {
        return Optional.empty();
    }

    @Override
    protected Optional<EInvoiceRefreshController> constructEInvoiceRefreshController() {
        return Optional.empty();
    }

    @Override
    protected Optional<TransferDestinationRefreshController> constructTransferDestinationRefreshController() {
        return Optional.empty();
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new AsLhvSessionHandler(this.apiClient);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
