package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.IngConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.IngAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.IngTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.IngTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.session.IngSessionHandler;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionMonthPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

import java.util.Optional;

public class IngAgent extends NextGenerationAgent {

    private final IngApiClient ingApiClient;

    public IngAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {

        super(request, context, signatureKeyPair);

        this.ingApiClient = new IngApiClient(this.client, sessionStorage);
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new IngAuthenticator(this.ingApiClient);
    }

    @Override
    protected Optional<TransactionalAccountRefreshController> constructTransactionalAccountRefreshController() {

        IngTransactionalAccountFetcher accountFetcher = new IngTransactionalAccountFetcher(ingApiClient, sessionStorage);
        IngTransactionFetcher transactionFetcher = new IngTransactionFetcher(ingApiClient);

        TransactionMonthPaginationController<TransactionalAccount> paginationController = new TransactionMonthPaginationController<>(
                transactionFetcher, IngConstants.ZONE_ID);

        TransactionFetcherController<TransactionalAccount> fetcherController = new TransactionFetcherController<>(
                transactionPaginationHelper, paginationController);

        TransactionalAccountRefreshController refreshController = new TransactionalAccountRefreshController(
                metricRefreshController, updateController, accountFetcher, fetcherController);

        return Optional.of(refreshController);
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
        return new IngSessionHandler(ingApiClient);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
