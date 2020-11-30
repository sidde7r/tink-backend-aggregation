package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import java.util.Locale;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator.KbcAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.KbcCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.KbcInvestmentAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.KbcTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.filters.KbcHttpFilter;
import se.tink.backend.aggregation.agents.progressive.ProgressiveAuthAgent;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.SubsequentGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.ProductionAgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.progressive.AutoAuthenticationProgressiveController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.ProgressiveAuthController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.SteppableAuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.SteppableAuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.TimeoutRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.credentials.service.CredentialsRequest;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS})
public final class KbcAgent
        extends SubsequentGenerationAgent<AutoAuthenticationProgressiveController>
        implements RefreshCreditCardAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshInvestmentAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                ProgressiveAuthAgent {

    private final KbcApiClient apiClient;
    private final String kbcLanguage;
    private KbcHttpFilter httpFilter;
    private final CreditCardRefreshController creditCardRefreshController;
    private final InvestmentRefreshController investmentRefreshController;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    private final AutoAuthenticationProgressiveController progressiveAuthenticator;

    public KbcAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(ProductionAgentComponentProvider.create(request, context, signatureKeyPair));
        configureHttpClient(client);
        kbcLanguage = getKbcLanguage(request.getUser().getLocale());

        this.apiClient = new KbcApiClient(client);

        this.creditCardRefreshController = constructCreditCardRefreshController();
        this.investmentRefreshController = constructInvestmentRefreshController();
        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();

        final KbcAuthenticator kbcAuthenticator =
                new KbcAuthenticator(
                        sessionStorage,
                        persistentStorage,
                        apiClient,
                        supplementalInformationFormer);
        final AutoAuthenticationProgressiveController autoAuthenticationController =
                new AutoAuthenticationProgressiveController(
                        request, systemUpdater, kbcAuthenticator, kbcAuthenticator);
        progressiveAuthenticator = autoAuthenticationController;
    }

    protected void configureHttpClient(TinkHttpClient client) {
        httpFilter = new KbcHttpFilter();
        client.addFilter(httpFilter);
        client.addFilter(
                new TimeoutRetryFilter(
                        KbcConstants.HttpClient.MAX_RETRIES,
                        KbcConstants.HttpClient.RETRY_SLEEP_MILLISECONDS,
                        HttpResponseException.class));
        client.setUserAgent(KbcConstants.Headers.USER_AGENT_VALUE);
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
        KbcTransactionalAccountFetcher accountFetcher =
                new KbcTransactionalAccountFetcher(apiClient, kbcLanguage, sessionStorage);
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(accountFetcher),
                        accountFetcher));
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
        KbcCreditCardFetcher creditCardFetcher =
                new KbcCreditCardFetcher(apiClient, sessionStorage);
        return new CreditCardRefreshController(
                metricRefreshController, updateController, creditCardFetcher, creditCardFetcher);
    }

    @Override
    public FetchInvestmentAccountsResponse fetchInvestmentAccounts() {
        return investmentRefreshController.fetchInvestmentAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchInvestmentTransactions() {
        return investmentRefreshController.fetchInvestmentTransactions();
    }

    private InvestmentRefreshController constructInvestmentRefreshController() {
        KbcInvestmentAccountFetcher accountFetcher =
                new KbcInvestmentAccountFetcher(apiClient, sessionStorage);
        return new InvestmentRefreshController(
                metricRefreshController, updateController, accountFetcher);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new KbcSessionHandler(httpFilter, apiClient, sessionStorage);
    }

    @Override
    public AutoAuthenticationProgressiveController getAuthenticator() {
        return progressiveAuthenticator;
    }

    private String getKbcLanguage(String locale) {
        if (Strings.isNullOrEmpty(locale)) {
            return Locale.ENGLISH.getLanguage();
        }
        if (locale.toLowerCase().contains(KbcConstants.LANGUAGE_DUTCH)) {
            return KbcConstants.LANGUAGE_DUTCH;
        }
        if (locale.toLowerCase().contains(Locale.FRENCH.getLanguage())) {
            return Locale.FRANCE.getLanguage();
        }
        if (locale.toLowerCase().contains(Locale.GERMAN.getLanguage())) {
            return Locale.GERMAN.getLanguage();
        }
        return Locale.ENGLISH.getLanguage();
    }

    @Override
    public SteppableAuthenticationResponse login(final SteppableAuthenticationRequest request)
            throws Exception {
        return ProgressiveAuthController.of(progressiveAuthenticator, credentials).login(request);
    }

    @Override
    public boolean login() {
        throw new AssertionError("ProgressiveAuthAgent::login should always be used");
    }
}
