package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.authenticator.SwedbankDefaultBankIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.SwedbankTransferHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.einvoice.SwedbankDefaultApproveEInvoiceExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.payment.SwedbankDefaultPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.transfer.SwedbankDefaultBankTransferExecutorNxgen;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.updatepayment.SwedbankDefaultUpdatePaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.creditcard.SwedbankDefaultCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.einvoice.SwedbankDefaultEinvoiceFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.investment.SwedbankDefaultInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.loan.SwedbankDefaultLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.transactional.SwedbankDefaultTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.transferdestination.SwedbankDefaultTransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.filters.SwedbankBaseHttpFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.interfaces.SwedbankApiClientProvider;
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
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;

public abstract class SwedbankAbstractAgentPaymentsRevamp extends NextGenerationAgent {
    protected final SwedbankConfiguration configuration;
    protected final SwedbankDefaultApiClient apiClient;

    public SwedbankAbstractAgentPaymentsRevamp(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair,
            SwedbankConfiguration configuration) {
        this(request, context, signatureKeyPair, configuration, new SwedbankDefaultApiClientProvider());
    }

    protected SwedbankAbstractAgentPaymentsRevamp(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair,
            SwedbankConfiguration configuration, SwedbankApiClientProvider apiClientProvider) {
        super(request, context, signatureKeyPair);
        this.configuration = configuration;
        this.apiClient = apiClientProvider.getApiAgent(client, configuration, credentials,
                sessionStorage);
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
        client.addFilter(new SwedbankBaseHttpFilter());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new BankIdAuthenticationController<>(context, new SwedbankDefaultBankIdAuthenticator(
                apiClient));
    }

    @Override
    protected Optional<TransactionalAccountRefreshController> constructTransactionalAccountRefreshController() {
        SwedbankDefaultTransactionalAccountFetcher transactionalFetcher =
                new SwedbankDefaultTransactionalAccountFetcher(apiClient, persistentStorage);

        TransactionFetcherController<TransactionalAccount> transactionFetcherController =
                new TransactionFetcherController<>(transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(transactionalFetcher), transactionalFetcher);

        return Optional.of(new TransactionalAccountRefreshController(metricRefreshController, updateController,
                transactionalFetcher, transactionFetcherController));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        SwedbankDefaultCreditCardFetcher creditCardFetcher = new SwedbankDefaultCreditCardFetcher(apiClient,
                request.getProvider().getCurrency());

        TransactionFetcherController<CreditCardAccount> transactionFetcherController =
                new TransactionFetcherController<>(transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(creditCardFetcher));

        return Optional.of(new CreditCardRefreshController(metricRefreshController, updateController,
                creditCardFetcher, transactionFetcherController));
    }

    @Override
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController() {
        SwedbankDefaultInvestmentFetcher investmentFetcher = new SwedbankDefaultInvestmentFetcher(apiClient,
                request.getProvider().getCurrency());

        return Optional.of(
                new InvestmentRefreshController(metricRefreshController, updateController, investmentFetcher));
    }

    @Override
    protected Optional<LoanRefreshController> constructLoanRefreshController() {
        return Optional.of(new LoanRefreshController(metricRefreshController, updateController,
                new SwedbankDefaultLoanFetcher(apiClient)));
    }

    @Override
    protected Optional<EInvoiceRefreshController> constructEInvoiceRefreshController() {
        return Optional.of(
                new EInvoiceRefreshController(metricRefreshController, updateController,
                        new SwedbankDefaultEinvoiceFetcher(apiClient)));
    }

    @Override
    protected Optional<TransferDestinationRefreshController> constructTransferDestinationRefreshController() {
        return Optional.of(
                new TransferDestinationRefreshController(metricRefreshController, updateController,
                        new SwedbankDefaultTransferDestinationFetcher(apiClient)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new SwedbankDefaultSessionHandler(apiClient);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        SwedbankTransferHelper transferHelper = new SwedbankTransferHelper(context, catalog,
                supplementalInformationHelper, apiClient);
        SwedbankDefaultBankTransferExecutorNxgen transferExecutor = new SwedbankDefaultBankTransferExecutorNxgen(
                catalog, apiClient, transferHelper);
        SwedbankDefaultPaymentExecutor paymentExecutor = new SwedbankDefaultPaymentExecutor(catalog, apiClient,
                transferHelper);
        SwedbankDefaultApproveEInvoiceExecutor approveEInvoiceExecutor = new SwedbankDefaultApproveEInvoiceExecutor(
                apiClient, transferHelper);
        SwedbankDefaultUpdatePaymentExecutor updatePaymentExecutor = new SwedbankDefaultUpdatePaymentExecutor(
                apiClient, transferHelper);
        return Optional.of(
                new TransferController(
                        paymentExecutor,
                        transferExecutor,
                        approveEInvoiceExecutor,
                        updatePaymentExecutor));
    }
}
