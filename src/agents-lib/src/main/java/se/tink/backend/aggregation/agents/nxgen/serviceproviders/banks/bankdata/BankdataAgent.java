package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.BankdataPinAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.BankdataCreditCardAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.BankdataCreditCardTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.BankdataInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.BankdataTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.BankdataTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.sessionhandler.BankdataSessionHandler;
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
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials_requests.CredentialsRequest;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;

public class BankdataAgent extends NextGenerationAgent {
    private BankdataApiClient bankClient;

    public BankdataAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        bankClient = new BankdataApiClient(client, request.getProvider());
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
        client.setDebugOutput(false);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(
                new BankdataPinAuthenticator(bankClient)
        );
    }

    @Override
    protected Optional<TransactionalAccountRefreshController> constructTransactionalAccountRefreshController() {
        BankdataTransactionFetcher transactionFetcher = new BankdataTransactionFetcher(bankClient);
        BankdataTransactionalAccountFetcher accountFetcher = new BankdataTransactionalAccountFetcher(bankClient);

        TransactionPagePaginationController<TransactionalAccount> transactionPagePaginationController = new
                TransactionPagePaginationController<>(
                transactionFetcher,
                BankdataConstants.Fetcher.START_PAGE
        );

        TransactionFetcherController<TransactionalAccount> transactionFetcherController =
                new TransactionFetcherController<>(transactionPaginationHelper,
                        transactionPagePaginationController, transactionFetcher);

        return Optional.of(
                new TransactionalAccountRefreshController(metricRefreshController, updateController, accountFetcher,
                        transactionFetcherController));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        BankdataCreditCardAccountFetcher ccAccountFetcher = new BankdataCreditCardAccountFetcher(bankClient);
        BankdataCreditCardTransactionFetcher ccTransactionFetcher =
                new BankdataCreditCardTransactionFetcher(bankClient);

        TransactionPagePaginationController<CreditCardAccount> ccTransactionPagePaginationController =
                new TransactionPagePaginationController<>(
                        ccTransactionFetcher,
                        BankdataConstants.Fetcher.START_PAGE
                );

        TransactionFetcherController<CreditCardAccount> ccTransactionFetcherController =
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        ccTransactionPagePaginationController
                );

        return Optional.of(
                new CreditCardRefreshController(
                        metricRefreshController,
                        updateController, 
                        ccAccountFetcher,
                        ccTransactionFetcherController));
    }

    @Override
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController() {
        BankdataInvestmentFetcher investmentFetcher = new BankdataInvestmentFetcher(bankClient);

        return Optional.of(new InvestmentRefreshController(metricRefreshController, updateController, investmentFetcher));
    }

    // have not found any loan data for this app
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
        return new BankdataSessionHandler();
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
