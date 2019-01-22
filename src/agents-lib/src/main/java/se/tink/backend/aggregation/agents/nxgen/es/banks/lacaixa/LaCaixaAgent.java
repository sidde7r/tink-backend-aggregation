package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.LaCaixaPasswordAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.LaCaixaCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.LaCaixaAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.LaCaixaTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.session.LaCaixaSessionHandler;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
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

public class LaCaixaAgent extends NextGenerationAgent {

    private final LaCaixaApiClient bankClient;

    public LaCaixaAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        bankClient = new LaCaixaApiClient(client);
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(
                new LaCaixaPasswordAuthenticator(bankClient)
        );
    }

    @Override
    protected Optional<TransactionalAccountRefreshController> constructTransactionalAccountRefreshController() {
        LaCaixaAccountFetcher accountFetcher = new LaCaixaAccountFetcher(bankClient);
        LaCaixaTransactionFetcher transactionFetcher = new LaCaixaTransactionFetcher(bankClient);

        return Optional.of(new TransactionalAccountRefreshController(metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(transactionPaginationHelper,
                        new TransactionPagePaginationController<>(transactionFetcher, 0))));

    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        LaCaixaCreditCardFetcher creditCardFetcher = new LaCaixaCreditCardFetcher(bankClient);
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
        return new LaCaixaSessionHandler(bankClient);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
