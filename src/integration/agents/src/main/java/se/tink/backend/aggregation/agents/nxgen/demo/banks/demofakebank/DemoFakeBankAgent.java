package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank.authenticator.DemoFakeBankAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank.fetcher.transactionalaccount.DemoFakeBankTransactionalAccountsFetcher;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank.sessionhandler.DemoFakeBankSessionHandler;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;

import java.util.Optional;

/* This is the agent for the Demo Fake Bank which is a Tink developed test & demo bank */

public final class DemoFakeBankAgent extends NextGenerationAgent {
    private final DemoFakeBankApiClient apiClient;
    private final SessionStorage sessionStorage;

    public DemoFakeBankAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        apiClient = new DemoFakeBankApiClient(client);
        sessionStorage = new SessionStorage();
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {}

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(
                new DemoFakeBankAuthenticator(apiClient, sessionStorage));
    }

    @Override
    protected Optional<TransactionalAccountRefreshController> constructTransactionalAccountRefreshController() {
        DemoFakeBankTransactionalAccountsFetcher transactionalAccountsFetcher =
                new DemoFakeBankTransactionalAccountsFetcher(apiClient, sessionStorage);

        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        transactionalAccountsFetcher,
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionKeyPaginationController<>(transactionalAccountsFetcher)
                        )
                )
        );
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
        return new DemoFakeBankSessionHandler(apiClient);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
