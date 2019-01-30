package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.accounts.checking.JyskeAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.accounts.checking.JyskeTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.accounts.creditcard.JyskeCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.accounts.creditcard.JyskeCreditCardTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.JyskeAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.JyskeKeyCardAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.investment.JyskeInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.session.JyskeSessionHandler;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardAuthenticationController;
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
import se.tink.libraries.credentials_requests.CredentialsRequest;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;

public class JyskeAgent extends NextGenerationAgent {
    private final JyskeApiClient apiClient;

    public JyskeAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        this.apiClient = new JyskeApiClient(client);
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
    }

    @Override
    protected Authenticator constructAuthenticator() {
        JyskePersistentStorage persistentStorage = new JyskePersistentStorage(this.persistentStorage);

        return new AutoAuthenticationController(request, context,
                new KeyCardAuthenticationController(catalog, supplementalInformationHelper,
                        new JyskeKeyCardAuthenticator(apiClient, persistentStorage)),
                new JyskeAutoAuthenticator(apiClient, persistentStorage, credentials));
    }

    @Override
    protected Optional<TransactionalAccountRefreshController> constructTransactionalAccountRefreshController() {
        JyskeTransactionFetcher transactionFetcher = new JyskeTransactionFetcher(apiClient);
        return Optional.of(new TransactionalAccountRefreshController(metricRefreshController, updateController,
                new JyskeAccountFetcher(apiClient),
                new TransactionFetcherController<>(transactionPaginationHelper,
                        new TransactionPagePaginationController<>(transactionFetcher, 0), transactionFetcher)));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        return Optional.of(new CreditCardRefreshController(metricRefreshController, updateController,
                new JyskeCreditCardFetcher(apiClient),
                new TransactionFetcherController<>(transactionPaginationHelper,
                        new TransactionPagePaginationController<>(new JyskeCreditCardTransactionFetcher(), 0))));
    }

    @Override
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController() {
        return Optional.of(new InvestmentRefreshController(metricRefreshController, updateController,
                new JyskeInvestmentFetcher(apiClient)));
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
        return new JyskeSessionHandler(apiClient, credentials);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
