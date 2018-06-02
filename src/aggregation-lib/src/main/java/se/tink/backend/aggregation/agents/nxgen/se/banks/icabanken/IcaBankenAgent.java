package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.IcaBankenBankIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.IcaBankenBankIdTransferController;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.IcaBankenBankIdTransferExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.Accounts.IcaBankenAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.investment.IcaBankenInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.loans.IcaBankenLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transactions.IcaBankenTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.IcaBankenTransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.rpc.IcaBankenSessionFilter;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
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
import se.tink.backend.aggregation.utils.transfer.StringNormalizerSwedish;
import se.tink.backend.aggregation.utils.transfer.TransferMessageFormatter;
import se.tink.backend.aggregation.utils.transfer.TransferMessageLengthConfig;

public class IcaBankenAgent extends NextGenerationAgent {
    private final IcaBankenApiClient apiClient;

    public IcaBankenAgent(CredentialsRequest request, AgentContext context) {
        super(request, context);
        this.apiClient = new IcaBankenApiClient(client);
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
        client.addFilter(new IcaBankenSessionFilter(sessionStorage));
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new BankIdAuthenticationController<>(context, new IcaBankenBankIdAuthenticator(apiClient,sessionStorage));
    }

    @Override
    protected Optional<TransactionalAccountRefreshController> constructTransactionalAccountRefreshController() {
        return Optional.of(new TransactionalAccountRefreshController(metricRefreshController,updateController,
                new IcaBankenAccountFetcher(apiClient),
                new TransactionFetcherController<>(transactionPaginationHelper,
                        new TransactionDatePaginationController<>(new IcaBankenTransactionFetcher(apiClient)))));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        return Optional.empty();
    }

    @Override
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController() {
        return Optional.of(new InvestmentRefreshController(metricRefreshController, updateController,
                new IcaBankenInvestmentFetcher(apiClient,sessionStorage)));
    }

    @Override
    protected Optional<LoanRefreshController> constructLoanRefreshController() {
        IcaBankenLoanFetcher loanFetcher = new IcaBankenLoanFetcher(apiClient);
        return Optional.of(new LoanRefreshController(metricRefreshController, updateController, loanFetcher));
    }

    @Override
    protected Optional<EInvoiceRefreshController> constructEInvoiceRefreshController() {
        return Optional.empty();
    }

    @Override
    protected Optional<TransferDestinationRefreshController> constructTransferDestinationRefreshController() {
        return Optional.of(new TransferDestinationRefreshController(metricRefreshController, updateController,
                new IcaBankenTransferDestinationFetcher(apiClient)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new IcaBankenSessionHandler(apiClient, sessionStorage);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.of(new IcaBankenBankIdTransferController(context,
                new IcaBankenBankIdTransferExecutor(apiClient, context,
                        new TransferMessageFormatter(catalog,
                TransferMessageLengthConfig.createWithMaxLength(14, 12),
                new StringNormalizerSwedish(",.-?!/+")),
                        new IcaBankenTransferDestinationFetcher(apiClient)),
                null,
                null,
                null));
    }

}
