package se.tink.backend.aggregation.nxgen.agents.demo;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;

public abstract class NextGenerationDemoAgent extends NextGenerationAgent {
    private final NextGenerationDemoAuthenticator authenticator;
    //TODO Requires changes when multi-currency is implemented. Will do for now
    private final String currency;

    public NextGenerationDemoAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        this.authenticator = new NextGenerationDemoAuthenticator(credentials);
        this.currency = request.getProvider().getCurrency();
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
        // NOOP
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new TypedAuthenticationController(
                new BankIdAuthenticationController<>(context, authenticator),
                new PasswordAuthenticationController(authenticator));
    }

    @Override
    protected Optional<TransactionalAccountRefreshController> constructTransactionalAccountRefreshController() {
        return Optional.of(new TransactionalAccountRefreshController(metricRefreshController, updateController,
                new NextGenerationDemoTransactionFetcher(request.getAccounts(), currency, catalog),
                new TransactionFetcherController<>(transactionPaginationHelper,
                        new NextGenerationDemoTransactionFetcher(request.getAccounts(), currency, catalog))));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        return Optional.empty();
    }

    @Override
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController() {
        return Optional.of(new InvestmentRefreshController(metricRefreshController,
                updateController,
                new NextGenerationDemoInvestmentFetcher(currency)));
    }

    @Override
    protected Optional<LoanRefreshController> constructLoanRefreshController() {
        return Optional.of(new LoanRefreshController(metricRefreshController,
                updateController,
                new NextGenerationDemoLoanFetcher(currency, catalog)));
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
        return new NextGenerationDemoSession();
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
