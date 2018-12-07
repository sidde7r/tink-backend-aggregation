package se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.sidentity;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.ErsteBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.fetcher.credit.ErsteBankCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.fetcher.transactional.ErsteBankAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.fetcher.transactional.ErsteBankTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.session.ErsteBankSessionHandler;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.sidentity.authenticator.ErstebankSidentityAuthenticator;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
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

public class ErstebankAgent extends NextGenerationAgent {

    private final ErsteBankApiClient ersteBankApiClient;

    public ErstebankAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        this.ersteBankApiClient = new ErsteBankApiClient(client, sessionStorage, persistentStorage);
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {

    }

    @Override
    protected Authenticator constructAuthenticator() {
        ErstebankSidentityAuthenticator authenticator =
                new ErstebankSidentityAuthenticator(ersteBankApiClient, supplementalInformationHelper);
        return new AutoAuthenticationController(request, context, authenticator, authenticator);
    }

    @Override
    protected Optional<TransactionalAccountRefreshController> constructTransactionalAccountRefreshController() {
        return Optional.of(
                new TransactionalAccountRefreshController(metricRefreshController, updateController,
                        new ErsteBankAccountFetcher(ersteBankApiClient),
                        new TransactionFetcherController<>(transactionPaginationHelper,
                                new TransactionPagePaginationController<>(
                                        new ErsteBankTransactionFetcher(ersteBankApiClient), 0))));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        ErsteBankCreditCardFetcher creditFetcher = new ErsteBankCreditCardFetcher(ersteBankApiClient);

        return Optional.of(new CreditCardRefreshController(metricRefreshController, updateController,
                creditFetcher,
                new TransactionFetcherController<>(transactionPaginationHelper,
                        new TransactionPagePaginationController<>(creditFetcher, 0))));
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
