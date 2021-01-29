package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demofinancialinstitution;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.LOANS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.MORTGAGE_AGGREGATION;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demofinancialinstitution.authenticator.DemoFinancialInstitutionAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demofinancialinstitution.configuration.DemoFinancialInstitutionConfiguration;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demofinancialinstitution.fetcher.creditcard.DemoFinancialInstitutionCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demofinancialinstitution.fetcher.identity.DemoFinancialInstitutionIdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demofinancialinstitution.fetcher.loan.DemoFinancialInstitutionLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demofinancialinstitution.fetcher.transactionalaccount.DemoFinancialInstitutionTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demofinancialinstitution.sessionhandler.DemoFinancialInstitutionSessionHandler;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;

/* This is the agent for the Demo Financial Institution which is a Tink developed test & demo bank */
@AgentCapabilities({
    CHECKING_ACCOUNTS,
    SAVINGS_ACCOUNTS,
    CREDIT_CARDS,
    IDENTITY_DATA,
    LOANS,
    MORTGAGE_AGGREGATION
})
public final class DemoFinancialInstitutionAgent extends NextGenerationAgent
        implements RefreshIdentityDataExecutor,
                RefreshLoanAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor {

    private final String clientName;
    private final DemoFinancialInstitutionApiClient apiClient;
    private final SessionStorage sessionStorage;

    private final LoanRefreshController loanRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public DemoFinancialInstitutionAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        clientName = request.getProvider().getPayload();
        sessionStorage = new SessionStorage();
        apiClient = new DemoFinancialInstitutionApiClient(client, sessionStorage);

        final DemoFinancialInstitutionLoanFetcher loanFetcher =
                new DemoFinancialInstitutionLoanFetcher(apiClient, sessionStorage);

        loanRefreshController =
                new LoanRefreshController(metricRefreshController, updateController, loanFetcher);

        creditCardRefreshController = constructCreditCardRefreshController();

        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();

        apiClient.setConfiguration(getClientConfiguration());
    }

    public DemoFinancialInstitutionConfiguration getClientConfiguration() {
        return getAgentConfigurationController()
                .getAgentConfigurationFromK8s(
                        DemoFinancialInstitutionConstants.INTEGRATION_NAME,
                        clientName,
                        DemoFinancialInstitutionConfiguration.class);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(
                new DemoFinancialInstitutionAuthenticator(sessionStorage, credentials, provider));
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
        final DemoFinancialInstitutionTransactionalAccountFetcher transactionalAccountsFetcher =
                new DemoFinancialInstitutionTransactionalAccountFetcher(apiClient, sessionStorage);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                transactionalAccountsFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(transactionalAccountsFetcher)));
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
        final DemoFinancialInstitutionCreditCardFetcher creditCardFetcher =
                new DemoFinancialInstitutionCreditCardFetcher(apiClient, sessionStorage);

        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                creditCardFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(creditCardFetcher)));
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
        return new DemoFinancialInstitutionSessionHandler(apiClient);
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        final IdentityDataFetcher fetcher = new DemoFinancialInstitutionIdentityDataFetcher();
        return new FetchIdentityDataResponse(fetcher.fetchIdentityData());
    }
}
