package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.INVESTMENTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.LOANS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import java.util.NoSuchElementException;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.SpankkiConstants.Authentication;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.SpankkiConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.SpankkiAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.SpankkiKeyCardAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.SpankkiSmsOtpAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.entities.CustomerEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.creditcard.SpankkiCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.creditcard.SpankkiCreditCardTransactionsFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.investment.SpankkiInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.loan.SpankkiLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.transactionalaccount.SpankkiTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.transactionalaccount.SpankkiTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.sessionhandler.SpankkiSessionHandler;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycardandsmsotp.KeyCardAndSmsOtpAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

@AgentCapabilities({
    CHECKING_ACCOUNTS,
    SAVINGS_ACCOUNTS,
    CREDIT_CARDS,
    INVESTMENTS,
    IDENTITY_DATA,
    LOANS
})
public final class SpankkiAgent extends NextGenerationAgent
        implements RefreshIdentityDataExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshInvestmentAccountsExecutor,
                RefreshLoanAccountsExecutor {
    private final SpankkiApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final InvestmentRefreshController investmentRefreshController;
    private final LoanRefreshController loanRefreshController;

    public SpankkiAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        apiClient = new SpankkiApiClient(client, persistentStorage, sessionStorage);

        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();

        creditCardRefreshController = constructCreditCardRefreshController();

        investmentRefreshController =
                new InvestmentRefreshController(
                        metricRefreshController,
                        updateController,
                        new SpankkiInvestmentFetcher(apiClient, persistentStorage));

        loanRefreshController =
                new LoanRefreshController(
                        metricRefreshController,
                        updateController,
                        new SpankkiLoanFetcher(apiClient));
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final KeyCardAndSmsOtpAuthenticationController mfaCtrl =
                new KeyCardAndSmsOtpAuthenticationController(
                        catalog,
                        supplementalInformationHelper,
                        new SpankkiKeyCardAuthenticator(
                                apiClient, persistentStorage, sessionStorage),
                        Authentication.KEY_CARD_VALUE_LENGTH,
                        new SpankkiSmsOtpAuthenticator(
                                apiClient, persistentStorage, sessionStorage),
                        Authentication.SMS_OTP_VALUE_LENGTH);

        return new AutoAuthenticationController(
                request,
                context,
                mfaCtrl,
                new SpankkiAutoAuthenticator(
                        apiClient, credentials, persistentStorage, sessionStorage));
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return transactionalAccountRefreshController.fetchCheckingAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return transactionalAccountRefreshController.fetchCheckingTransactions();
    }

    @Override
    public FetchAccountsResponse fetchSavingsAccounts() {
        return transactionalAccountRefreshController.fetchSavingsAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchSavingsTransactions() {
        return transactionalAccountRefreshController.fetchSavingsTransactions();
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return creditCardRefreshController.fetchCreditCardTransactions();
    }

    @Override
    public FetchInvestmentAccountsResponse fetchInvestmentAccounts() {
        return investmentRefreshController.fetchInvestmentAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchInvestmentTransactions() {
        return investmentRefreshController.fetchInvestmentTransactions();
    }

    @Override
    public FetchLoanAccountsResponse fetchLoanAccounts() {
        return loanRefreshController.fetchLoanAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchLoanTransactions() {
        return loanRefreshController.fetchLoanTransactions();
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new SpankkiSessionHandler(apiClient);
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        // There is also the endpoint "/v2/core/customer/profile" that can also fetch identity data
        return persistentStorage
                .get(Storage.CUSTOMER_ENTITY, CustomerEntity.class)
                .map(CustomerEntity::toTinkIdentity)
                .map(FetchIdentityDataResponse::new)
                .orElseThrow(NoSuchElementException::new);
    }

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new SpankkiTransactionalAccountFetcher(apiClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionPagePaginationController<>(
                                new SpankkiTransactionFetcher(apiClient), 0)));
    }

    private CreditCardRefreshController constructCreditCardRefreshController() {
        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                new SpankkiCreditCardFetcher(apiClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(
                                        new SpankkiCreditCardTransactionsFetcher(apiClient))
                                .build()));
    }
}
