package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.LOANS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.authenticator.SparebankenSorAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.authenticator.SparebankenSorMultiFactorAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.authenticator.rpc.FirstLoginRequest;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.creditcard.SparebankenSorCreditCardAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.creditcard.SparebankenSorCreditCardTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.loan.SparebankenSorLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.transactionalaccount.SparebankenSorTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.transactionalaccount.SparebankenSorTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.filters.AddRefererFilter;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.EncapClient;
import se.tink.backend.aggregation.agents.utils.encoding.messagebodywriter.NoEscapeOfBackslashMessageBodyWriter;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.bankid.BankIdAuthenticationControllerNO;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

/**
 * WIP! This provider is dependant on us being able to trigger supplemental information twice, which
 * doesn't work with the current version of the app. Haven't added the provider to the provider
 * config just to be certain that it doesn't accidentally end up in the app.
 *
 * <p>Things left to do before it can be used in production: - Assert that registration works
 * (activation flow) - Assert that registered user can log in (authentication flow) - Investigate
 * investment fetching, 2018-02-13 they seemed to route to the netbank - Add provider to provider
 * config - Add rules to appstore monitor
 */
@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS, LOANS})
public final class SparebankenSorAgent extends NextGenerationAgent
        implements RefreshLoanAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor {
    private final SparebankenSorApiClient apiClient;
    private final EncapClient encapClient;
    private final LoanRefreshController loanRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    @Inject
    public SparebankenSorAgent(AgentComponentProvider agentComponentProvider) {
        super(agentComponentProvider);
        configureHttpClient(client);
        apiClient = new SparebankenSorApiClient(client, sessionStorage);

        this.encapClient =
                agentComponentProvider.getEncapClient(
                        persistentStorage,
                        new SparebankenSorEncapConfiguration(),
                        SparebankenSorConstants.DEVICE_PROFILE,
                        client);

        SparebankenSorLoanFetcher loanFetcher = new SparebankenSorLoanFetcher(apiClient);
        loanRefreshController =
                new LoanRefreshController(metricRefreshController, updateController, loanFetcher);

        creditCardRefreshController = constructCreditCardRefreshController();
        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
    }

    protected void configureHttpClient(TinkHttpClient client) {

        AddRefererFilter filter = new AddRefererFilter();
        client.addFilter(filter);
        client.addMessageWriter(new NoEscapeOfBackslashMessageBodyWriter(FirstLoginRequest.class));
    }

    @Override
    protected Authenticator constructAuthenticator() {

        SparebankenSorMultiFactorAuthenticator multiFactorAuthenticator =
                new SparebankenSorMultiFactorAuthenticator(
                        apiClient,
                        encapClient,
                        supplementalInformationHelper,
                        sessionStorage,
                        credentials.getField(Field.Key.MOBILENUMBER),
                        catalog);

        SparebankenSorAutoAuthenticator autoAuthenticator =
                new SparebankenSorAutoAuthenticator(apiClient, encapClient, sessionStorage);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new BankIdAuthenticationControllerNO(
                        supplementalRequester, multiFactorAuthenticator, catalog),
                autoAuthenticator);
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

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new SparebankenSorTransactionalAccountFetcher(apiClient),
                new SparebankenSorTransactionFetcher(apiClient));
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return creditCardRefreshController.fetchCreditCardTransactions();
    }

    private CreditCardRefreshController constructCreditCardRefreshController() {
        SparebankenSorCreditCardAccountFetcher ccAccountFetcher =
                new SparebankenSorCreditCardAccountFetcher(apiClient);
        SparebankenSorCreditCardTransactionFetcher ccTransactionFetcher =
                new SparebankenSorCreditCardTransactionFetcher();

        return new CreditCardRefreshController(
                metricRefreshController, updateController, ccAccountFetcher, ccTransactionFetcher);
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
        return new SparebankenSorSessionHandler();
    }
}
