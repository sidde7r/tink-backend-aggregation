package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken;

import java.util.Optional;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.IcaBankenBankIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.IcaBankenBankTransferExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.IcaBankenExecutorHelper;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.IcaBankenPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.einvoice.IcaBankenEInvoiceExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.IcaBankenCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.IcaBankenTransactionalAccountsFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.einvoice.IcaBankenEInvoiceFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.identitydata.IcaBankenIdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.investment.IcaBankenInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.loans.IcaBankenLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.IcaBankenTransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.filter.IcaBankenFilter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.storage.IcaBankenSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.storage.IcabankenPersistentStorage;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
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
import se.tink.libraries.credentials.service.CredentialsRequest;

public class IcaBankenAgent extends NextGenerationAgent implements RefreshIdentityDataExecutor {

    private final IcaBankenApiClient apiClient;
    private final IcaBankenSessionStorage icaBankenSessionStorage;
    private final IcabankenPersistentStorage icaBankenPersistentStorage;

    public IcaBankenAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        configureHttpClient(client);
        this.icaBankenSessionStorage = new IcaBankenSessionStorage(sessionStorage);
        this.icaBankenPersistentStorage = new IcabankenPersistentStorage(persistentStorage);
        this.apiClient =
                new IcaBankenApiClient(client, icaBankenSessionStorage, icaBankenPersistentStorage);
    }

    protected void configureHttpClient(TinkHttpClient client) {
        client.addFilter(new IcaBankenFilter());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new BankIdAuthenticationController<>(
                supplementalRequester,
                new IcaBankenBankIdAuthenticator(
                        apiClient, icaBankenSessionStorage, icaBankenPersistentStorage));
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        IcaBankenTransactionalAccountsFetcher transactionalAccountFetcher =
                new IcaBankenTransactionalAccountsFetcher(apiClient);

        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        transactionalAccountFetcher,
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionKeyPaginationController<>(
                                        transactionalAccountFetcher),
                                transactionalAccountFetcher)));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        IcaBankenCreditCardFetcher creditCardFetcher = new IcaBankenCreditCardFetcher(apiClient);

        return Optional.of(
                new CreditCardRefreshController(
                        metricRefreshController,
                        updateController,
                        creditCardFetcher,
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionKeyPaginationController<>(creditCardFetcher))));
    }

    @Override
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController() {
        return Optional.of(
                new InvestmentRefreshController(
                        metricRefreshController,
                        updateController,
                        new IcaBankenInvestmentFetcher(apiClient)));
    }

    @Override
    protected Optional<LoanRefreshController> constructLoanRefreshController() {

        return Optional.of(
                new LoanRefreshController(
                        metricRefreshController,
                        updateController,
                        new IcaBankenLoanFetcher(apiClient)));
    }

    @Override
    protected Optional<EInvoiceRefreshController> constructEInvoiceRefreshController() {
        IcaBankenEInvoiceFetcher eInvoiceFetcher = new IcaBankenEInvoiceFetcher(apiClient, catalog);

        return Optional.of(new EInvoiceRefreshController(metricRefreshController, eInvoiceFetcher));
    }

    @Override
    protected Optional<TransferDestinationRefreshController>
            constructTransferDestinationRefreshController() {
        return Optional.of(
                new TransferDestinationRefreshController(
                        metricRefreshController,
                        new IcaBankenTransferDestinationFetcher(apiClient)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new IcaBankenSessionHandler(apiClient, icaBankenSessionStorage);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        IcaBankenExecutorHelper executorHelper =
                new IcaBankenExecutorHelper(
                        apiClient, context, catalog, supplementalInformationHelper);

        return Optional.of(
                new TransferController(
                        new IcaBankenPaymentExecutor(apiClient, executorHelper),
                        new IcaBankenBankTransferExecutor(apiClient, executorHelper, catalog),
                        new IcaBankenEInvoiceExecutor(apiClient, executorHelper, catalog),
                        null));
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        IcaBankenIdentityDataFetcher identityDataFetcher =
                new IcaBankenIdentityDataFetcher(
                        apiClient, credentials.getField(Field.Key.USERNAME));
        return identityDataFetcher.getIdentityDataResponse();
    }
}
