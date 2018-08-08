package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.CommerzbankPasswordAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.account.CommerzbankAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction.CommerzbankTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.session.CommerzbankSessionHandler;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.index.TransactionIndexPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.rpc.CredentialsRequest;

public class CommerzbankAgent extends NextGenerationAgent {

    private final CommerzbankApiClient apiClient;

    public CommerzbankAgent(CredentialsRequest request, AgentContext context) {
        super(request, context);
        apiClient = new CommerzbankApiClient(client, sessionStorage);
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(
                new CommerzbankPasswordAuthenticator(apiClient, sessionStorage));
    }

    @Override
    protected Optional<TransactionalAccountRefreshController> constructTransactionalAccountRefreshController() {
        return Optional.of(new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new CommerzbankAccountFetcher(apiClient, sessionStorage),
                new TransactionFetcherController<>(this.transactionPaginationHelper,
                        new TransactionIndexPaginationController<>(new CommerzbankTransactionFetcher(apiClient)))));
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
        return new CommerzbankSessionHandler(apiClient);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
