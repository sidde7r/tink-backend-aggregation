package se.tink.backend.aggregation.agents.nxgen.es.banks.ing;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.authenticator.IngAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.fetcher.IngTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.session.IngSessionHandler;
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
import se.tink.libraries.credentials.service.CredentialsRequest;

public class IngAgent extends NextGenerationAgent {

    private final IngApiClient apiClient;

    public IngAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        apiClient = new IngApiClient(client);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new IngAuthenticator(apiClient, sessionStorage);
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {

        IngTransactionalAccountFetcher fetcher = new IngTransactionalAccountFetcher(apiClient);

        TransactionMonthPaginationController<TransactionalAccount> paginationController =
                new TransactionMonthPaginationController<>(fetcher, IngConstants.ZONE_ID);

        TransactionFetcherController<TransactionalAccount> fetcherController =
                new TransactionFetcherController<>(
                        transactionPaginationHelper, paginationController);

        TransactionalAccountRefreshController refreshController =
                new TransactionalAccountRefreshController(
                        metricRefreshController, updateController, fetcher, fetcherController);

        return Optional.of(refreshController);
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {

        //        IngCreditCardFetcher fetcher = new IngCreditCardFetcher(apiClient);
        //
        //        TransactionMonthPaginationController<CreditCardAccount> paginationController = new
        // TransactionMonthPaginationController<>(
        //                fetcher, IngConstants.ZONE_ID);
        //
        //        TransactionFetcherController<CreditCardAccount> fetcherController = new
        // TransactionFetcherController<>(
        //                transactionPaginationHelper, paginationController);
        //
        //        CreditCardRefreshController refreshController = new CreditCardRefreshController(
        //                metricRefreshController, updateController, fetcher, fetcherController);
        //
        //        return Optional.of(refreshController);
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
        return new IngSessionHandler(apiClient, sessionStorage);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
