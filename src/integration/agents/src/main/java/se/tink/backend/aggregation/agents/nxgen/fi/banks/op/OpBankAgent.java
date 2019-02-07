package se.tink.backend.aggregation.agents.nxgen.fi.banks.op;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.OpAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.OpAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.OpBankCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.OpBankInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.OpBankLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.OpBankTransactionalAccountsFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.sessionhandler.OpBankSessionHandler;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;

public class OpBankAgent extends NextGenerationAgent {

    private final OpBankApiClient bankClient;
    private OpBankPersistentStorage opBankPersistentStorage;


    public OpBankAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        bankClient = new OpBankApiClient(client);
        this.opBankPersistentStorage = new OpBankPersistentStorage(credentials, persistentStorage);;
    }


    @Override
    public void configureHttpClient(TinkHttpClient client) {
    }

    @Override
    public Authenticator constructAuthenticator() {
        return new AutoAuthenticationController(request, context,
                new KeyCardAuthenticationController(catalog,
                        supplementalInformationHelper,
                        new OpAuthenticator(bankClient, opBankPersistentStorage, credentials),
                        OpBankConstants.KEYCARD_PIN_LENGTH),
                new OpAutoAuthenticator(bankClient, opBankPersistentStorage, credentials));
    }

    @Override
    public Optional<TransactionalAccountRefreshController> constructTransactionalAccountRefreshController() {
        return Optional.of(new TransactionalAccountRefreshController(metricRefreshController, updateController,
                new OpBankTransactionalAccountsFetcher(bankClient),
                new TransactionFetcherController<>(transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(new OpBankTransactionalAccountsFetcher(bankClient)))));
    }

    @Override
    public Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        OpBankCreditCardFetcher creditCardFetcher = new OpBankCreditCardFetcher(bankClient, credentials);
        TransactionFetcher<CreditCardAccount> creditCardTransactionFetcher = new TransactionFetcherController<>(
                transactionPaginationHelper,
                new TransactionDatePaginationController<>(creditCardFetcher));

        return Optional.of(new CreditCardRefreshController(metricRefreshController, updateController, creditCardFetcher,
                creditCardTransactionFetcher));
    }

    @Override
    public Optional<InvestmentRefreshController> constructInvestmentRefreshController() {
        return Optional.of(new InvestmentRefreshController(metricRefreshController, updateController,
                new OpBankInvestmentFetcher(bankClient, credentials)));
    }

    @Override
    public Optional<LoanRefreshController> constructLoanRefreshController() {
        return Optional.of(new LoanRefreshController(metricRefreshController, updateController,
                new OpBankLoanFetcher(bankClient, credentials)));
    }

    @Override
    public Optional<EInvoiceRefreshController> constructEInvoiceRefreshController() {
        return Optional.empty();
    }

    @Override
    public Optional<TransferDestinationRefreshController> constructTransferDestinationRefreshController() {
        return Optional.empty();
    }

    @Override
    public SessionHandler constructSessionHandler() {
        return new OpBankSessionHandler(bankClient);
    }

    @Override
    public Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
