package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.IcaBankenBankIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.IcaBankenBankIdTransferController;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.IcaBankenBankIdTransferExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.einvoice.IcaBankenApproveEInvoiceExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.einvoice.IcaBankenEInvoiceExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.IcaBankenCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.IcaBankenTransactionalAccountsFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.einvoice.IcaBankenEInvoiceFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.investment.IcaBankenInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.loans.IcaBankenLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.IcaBankenTransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.filter.IcaBankenFilter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.storage.IcaBankenSessionStorage;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
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
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.utils.transfer.StringNormalizerSwedish;
import se.tink.backend.aggregation.utils.transfer.TransferMessageFormatter;
import se.tink.backend.aggregation.utils.transfer.TransferMessageLengthConfig;
import se.tink.backend.common.config.SignatureKeyPair;

public class IcaBankenAgent extends NextGenerationAgent {
    private final IcaBankenApiClient apiClient;
    private final TransferMessageFormatter transferMessageFormatter = new TransferMessageFormatter(catalog,
            TransferMessageLengthConfig.createWithMaxLength(14, 12), new StringNormalizerSwedish(",.-?!/+"));
    private final IcaBankenSessionStorage icaBankenSessionStorage;

    public IcaBankenAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        this.icaBankenSessionStorage = new IcaBankenSessionStorage(sessionStorage);
        this.apiClient = new IcaBankenApiClient(client, icaBankenSessionStorage);
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
        client.addFilter(new IcaBankenFilter());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new BankIdAuthenticationController<>(context,
                new IcaBankenBankIdAuthenticator(apiClient, icaBankenSessionStorage));
    }

    @Override
    protected Optional<TransactionalAccountRefreshController> constructTransactionalAccountRefreshController() {
        IcaBankenTransactionalAccountsFetcher transactionalAccountFetcher =
                new IcaBankenTransactionalAccountsFetcher(apiClient);

        return Optional.of(new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        transactionalAccountFetcher,
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionKeyPaginationController<>(transactionalAccountFetcher),
                                transactionalAccountFetcher)
                )
        );
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        IcaBankenCreditCardFetcher creditCardFetcher = new IcaBankenCreditCardFetcher(apiClient);

        return Optional.of(new CreditCardRefreshController(
                        metricRefreshController,
                        updateController,
                        creditCardFetcher,
                        new TransactionFetcherController<>(transactionPaginationHelper,
                                new TransactionKeyPaginationController<>(creditCardFetcher))
                )
        );
    }

    @Override
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController() {
        return Optional.of(new InvestmentRefreshController(
                        metricRefreshController,
                        updateController,
                        new IcaBankenInvestmentFetcher(apiClient)
                )
        );
    }

    @Override
    protected Optional<LoanRefreshController> constructLoanRefreshController() {

        return Optional.of(new LoanRefreshController(
                        metricRefreshController,
                        updateController,
                        new IcaBankenLoanFetcher(apiClient)
                )
        );
    }

    @Override
    protected Optional<EInvoiceRefreshController> constructEInvoiceRefreshController() {
        IcaBankenEInvoiceFetcher eInvoiceFetcher = new IcaBankenEInvoiceFetcher(apiClient, catalog);

        return Optional.of(new EInvoiceRefreshController(
                        metricRefreshController,
                        updateController,
                        eInvoiceFetcher)
        );
    }

    @Override
    protected Optional<TransferDestinationRefreshController> constructTransferDestinationRefreshController() {
        return Optional.of(new TransferDestinationRefreshController(metricRefreshController, updateController,
                new IcaBankenTransferDestinationFetcher(apiClient)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new IcaBankenSessionHandler(apiClient, icaBankenSessionStorage);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.of(new IcaBankenBankIdTransferController(context,
                new IcaBankenBankIdTransferExecutor(apiClient, context, transferMessageFormatter), null,
                new IcaBankenApproveEInvoiceExecutor(apiClient, context, catalog, transferMessageFormatter,
                        new IcaBankenEInvoiceFetcher(apiClient, catalog, context)), null));
    }
}
