package se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.authenticator.IberCajaPasswordAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount.IberCajaAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount.IberCajaCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount.IberCajaCreditCardTransactionalFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount.IberCajaInvestmentAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount.IberCajaTransactionalFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount.session.IberCajaSessionHandler;
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
import se.tink.backend.aggregation.configuration.SignatureKeyPair;

public class IberCajaAgent extends NextGenerationAgent {

    private final IberCajaApiClient apiClient;

    public IberCajaAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        apiClient = new IberCajaApiClient(client, sessionStorage);
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {

        //client.setDebugProxy("http://127.0.0.1:8888");
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(
                new IberCajaPasswordAuthenticator(apiClient, sessionStorage)
        );
    }

    @Override
    protected Optional<TransactionalAccountRefreshController> constructTransactionalAccountRefreshController() {
        IberCajaAccountFetcher accountFetcher = new IberCajaAccountFetcher(apiClient);
        IberCajaTransactionalFetcher transactionalFetcher = new IberCajaTransactionalFetcher(apiClient);

        return Optional.of(new TransactionalAccountRefreshController(metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(transactionPaginationHelper,
                        new TransactionDatePaginationController<>(transactionalFetcher))
        ));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        IberCajaCreditCardFetcher creditCardFetcher = new IberCajaCreditCardFetcher(apiClient);
        IberCajaCreditCardTransactionalFetcher transactionalFetcher =
                new IberCajaCreditCardTransactionalFetcher(apiClient);

        return Optional.of(new CreditCardRefreshController(metricRefreshController,
                updateController,
                creditCardFetcher,
                new TransactionFetcherController<>(transactionPaginationHelper,
                        new TransactionDatePaginationController<>(transactionalFetcher))

        ));
    }

    @Override
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController() {

        IberCajaInvestmentAccountFetcher investmentAccountFetcher = new IberCajaInvestmentAccountFetcher(apiClient);
        return Optional.of(new InvestmentRefreshController(metricRefreshController, updateController,
                investmentAccountFetcher));
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
        return new IberCajaSessionHandler(apiClient);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
