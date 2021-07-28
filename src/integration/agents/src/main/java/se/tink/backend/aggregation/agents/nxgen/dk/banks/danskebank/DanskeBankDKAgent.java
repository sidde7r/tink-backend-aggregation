package se.tink.backend.aggregation.agents.nxgen.dk.banks.danskebank;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.INVESTMENTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.LOANS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.MORTGAGE_AGGREGATION;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
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
import se.tink.backend.aggregation.agents.nxgen.dk.banks.danskebank.fetcher.identitydata.DanskeBankDKIdentityFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants.HttpClientParams;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.DanskeBankChallengeAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.DanskeBankAccountLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.DanskeBankCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.DanskeBankTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.mapper.AccountEntityMarketMapper;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorage;

@AgentCapabilities({
    CHECKING_ACCOUNTS,
    SAVINGS_ACCOUNTS,
    CREDIT_CARDS,
    INVESTMENTS,
    IDENTITY_DATA,
    LOANS,
    MORTGAGE_AGGREGATION
})
public final class DanskeBankDKAgent extends DanskeBankAgent<DanskeBankDKApiClient>
        implements RefreshLoanAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshIdentityDataExecutor {

    private final AgentTemporaryStorage agentTemporaryStorage;
    private final LoanRefreshController loanRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    @Inject
    public DanskeBankDKAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, new AccountEntityMarketMapper("DK"));
        this.agentTemporaryStorage = componentProvider.getAgentTemporaryStorage();
        // DK fetches loans at a separate loan endpoint
        this.loanRefreshController =
                new LoanRefreshController(
                        this.metricRefreshController,
                        this.updateController,
                        new DanskeBankAccountLoanFetcher(
                                apiClient,
                                configuration,
                                accountEntityMapper,
                                true,
                                accountDetailsFetcher));

        LocalDateTimeSource localDateTimeSource = componentProvider.getLocalDateTimeSource();
        this.creditCardRefreshController =
                constructCreditCardRefreshController(localDateTimeSource);
        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController(localDateTimeSource);
        this.client.setTimeout(HttpClientParams.CLIENT_TIMEOUT);
    }

    @Override
    protected DanskeBankConfiguration createConfiguration() {
        return new DanskeBankDKConfiguration(catalog);
    }

    @Override
    protected DanskeBankDKApiClient createApiClient(
            TinkHttpClient client, DanskeBankConfiguration configuration) {
        return new DanskeBankDKApiClient(
                client, (DanskeBankDKConfiguration) configuration, credentials, catalog);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        DanskeBankChallengeAuthenticator danskeBankChallengeAuthenticator =
                new DanskeBankChallengeAuthenticator(
                        catalog,
                        supplementalInformationController,
                        apiClient,
                        client,
                        persistentStorage,
                        credentials,
                        deviceId,
                        configuration,
                        agentTemporaryStorage);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                danskeBankChallengeAuthenticator,
                danskeBankChallengeAuthenticator);
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

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController(
            LocalDateTimeSource localDateTimeSource) {
        return new TransactionalAccountRefreshController(
                this.metricRefreshController,
                this.updateController,
                new DanskeBankTransactionalAccountFetcher(
                        this.apiClient,
                        this.configuration,
                        accountEntityMapper,
                        accountDetailsFetcher),
                createTransactionFetcherController(localDateTimeSource));
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return creditCardRefreshController.fetchCreditCardTransactions();
    }

    private CreditCardRefreshController constructCreditCardRefreshController(
            LocalDateTimeSource localDateTimeSource) {
        return new CreditCardRefreshController(
                this.metricRefreshController,
                this.updateController,
                new DanskeBankCreditCardFetcher(
                        this.apiClient,
                        this.configuration,
                        accountEntityMapper,
                        accountDetailsFetcher),
                createTransactionFetcherController(localDateTimeSource));
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return new FetchIdentityDataResponse(
                new DanskeBankDKIdentityFetcher(persistentStorage).fetchIdentityData());
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }
}
