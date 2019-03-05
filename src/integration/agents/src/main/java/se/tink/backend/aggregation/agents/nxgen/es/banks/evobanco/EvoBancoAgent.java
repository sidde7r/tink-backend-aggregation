package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.EvoBancoPasswordAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.transactionalaccount.EvoBancoAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.transactionalaccount.EvoBancoTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.session.EvoBancoSessionHandler;
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
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

import java.util.Optional;

public class EvoBancoAgent extends NextGenerationAgent {

    private final EvoBancoApiClient bankClient;

    public EvoBancoAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        bankClient = new EvoBancoApiClient(client, sessionStorage);
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
        client.setDebugProxy("http://192.168.238.15:8888");
        client.setDebugOutput(true);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(
                new EvoBancoPasswordAuthenticator(bankClient, sessionStorage));
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        EvoBancoAccountFetcher accountFetcher = new EvoBancoAccountFetcher(bankClient, sessionStorage);
        TransactionFetcherController<TransactionalAccount> transactionFetcher =
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(
                                new EvoBancoTransactionFetcher(bankClient, sessionStorage)));

        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        accountFetcher,
                        transactionFetcher));
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
        return new EvoBancoSessionHandler(bankClient, sessionStorage);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
