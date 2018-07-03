package se.tink.backend.aggregation.agents.nxgen.de.banks.fints;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.accounts.checking.FinTsAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.accounts.checking.FinTsTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.authenticator.FinTsAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.session.FinTsSessionHandler;
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
import se.tink.backend.aggregation.rpc.Field;

public class FinTsAgent extends NextGenerationAgent {

    private FinTsApiClient apiClient;

    public FinTsAgent(CredentialsRequest request, AgentContext context) {
        super(request, context);
        // TODO, get blz and endpoint from provider configuration when provider conf is finished
        FinTsConfiguration configuration =
                new FinTsConfiguration(
                        "20070000",
                        "https://fints.deutsche-bank.de",
                        request.getCredentials().getField(Field.Key.USERNAME),
                        request.getCredentials().getField(Field.Key.PASSWORD));

        this.apiClient = new FinTsApiClient(this.client, configuration);
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {

    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(new FinTsAuthenticator(apiClient));
    }

    @Override
    protected Optional<TransactionalAccountRefreshController> constructTransactionalAccountRefreshController() {
        FinTsTransactionFetcher transactionFetcher = new FinTsTransactionFetcher(apiClient);
        return Optional.of(new TransactionalAccountRefreshController(metricRefreshController, updateController,
                new FinTsAccountFetcher(apiClient),
                new TransactionFetcherController<>(this.transactionPaginationHelper,
                        new TransactionDatePaginationController<>(transactionFetcher))));
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
    protected Optional<TransferDestinationRefreshController>
    constructTransferDestinationRefreshController() {
        return Optional.empty();
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new FinTsSessionHandler(apiClient);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
