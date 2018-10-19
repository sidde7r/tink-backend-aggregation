package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.authenticator.ImaginBankPasswordAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.creditcard.ImaginBankCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.creditcard.rpc.CardTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.transactionalaccount.ImaginBankAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.transactionalaccount.ImaginBankTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.session.ImaginBankSessionHandler;
import se.tink.backend.aggregation.agents.utils.encoding.messagebodywriter.NoEscapeOfBackslashMessageBodyWriter;
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
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.common.config.SignatureKeyPair;

/*
 * This agent is to great extents a copy of lacaixa agent.
 * The main differences are authentication.
 * ImaginBank also has separate account fetching
 */
public class ImaginBankAgent extends NextGenerationAgent {

    private final ImaginBankApiClient apiClient;
    private final ImaginBankSessionStorage imaginBankSessionStorage;

    public ImaginBankAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        apiClient = new ImaginBankApiClient(client);
        imaginBankSessionStorage = new ImaginBankSessionStorage(sessionStorage);
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
        client.addMessageWriter(new NoEscapeOfBackslashMessageBodyWriter(CardTransactionsRequest.class));
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(
                new ImaginBankPasswordAuthenticator(apiClient, imaginBankSessionStorage)
        );
    }

    @Override
    protected Optional<TransactionalAccountRefreshController> constructTransactionalAccountRefreshController() {
        ImaginBankAccountFetcher accountFetcher = new ImaginBankAccountFetcher(apiClient, imaginBankSessionStorage);
        ImaginBankTransactionFetcher transactionFetcher = new ImaginBankTransactionFetcher(apiClient);

        return Optional.of(new TransactionalAccountRefreshController(metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(transactionPaginationHelper,
                        new TransactionPagePaginationController<>(transactionFetcher, 0))));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        ImaginBankCreditCardFetcher creditCardFetcher = new ImaginBankCreditCardFetcher(apiClient);

        return Optional.of(new CreditCardRefreshController(metricRefreshController, updateController,
                creditCardFetcher, new TransactionFetcherController<>(this.transactionPaginationHelper,
                new TransactionPagePaginationController<>(creditCardFetcher, 0))));
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
        return new ImaginBankSessionHandler(apiClient);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
