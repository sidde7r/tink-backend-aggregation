package se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.authenticator.ErsteBankPasswordAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.fetcher.ErsteBankAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.fetcher.ErsteBankTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.session.ErsteBankSessionHandler;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.rpc.CredentialsRequest;

public class ErsteBankAgent extends NextGenerationAgent{

    private final ErsteBankApiClient ersteBankApiClient;
    private final SessionStorage storage;

    public ErsteBankAgent(CredentialsRequest request, AgentContext context){
        super(request, context);
        storage = new SessionStorage();
        this.ersteBankApiClient = new ErsteBankApiClient(this.client, storage);
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(new ErsteBankPasswordAuthenticator(ersteBankApiClient));
    }

    @Override
    protected Optional<TransactionalAccountRefreshController> constructTransactionalAccountRefreshController() {
        return Optional.of(
                new TransactionalAccountRefreshController(metricRefreshController, updateController,
                        new ErsteBankAccountFetcher(ersteBankApiClient),
                        new TransactionFetcherController<>(transactionPaginationHelper,
                                new TransactionPagePaginationController<>(new ErsteBankTransactionFetcher(ersteBankApiClient), 0))));
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
        return new ErsteBankSessionHandler(ersteBankApiClient);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
