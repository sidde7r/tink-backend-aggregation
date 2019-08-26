package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.ManualOrAutoAuth;
import se.tink.backend.aggregation.agents.ProgressiveAuthAgent;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator.KbcAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.executor.KbcBankTransferExecutor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.KbcCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.KbcTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.KbcTransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.filters.KbcHttpFilter;
import se.tink.backend.aggregation.annotations.ProgressiveAuth;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.ProgressiveAuthController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.ProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SteppableAuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SteppableAuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

@ProgressiveAuth
public final class KbcAgent extends NextGenerationAgent
        implements RefreshTransferDestinationExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                ProgressiveAuthAgent,
                ManualOrAutoAuth {

    private final KbcApiClient apiClient;
    private final String kbcLanguage;
    private KbcHttpFilter httpFilter;
    private final TransferDestinationRefreshController transferDestinationRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    private final Authenticator authenticator; // TODO remove when decoupled from NxgenAgent
    private final ManualOrAutoAuth manualOrAutoAuthAuthenticator;

    public KbcAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        configureHttpClient(client);
        kbcLanguage = getKbcLanguage(request.getUser().getLocale());
        this.apiClient = new KbcApiClient(client);

        this.transferDestinationRefreshController = constructTransferDestinationRefreshController();
        this.creditCardRefreshController = constructCreditCardRefreshController();
        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();

        final KbcAuthenticator kbcAuthenticator =
                new KbcAuthenticator(
                        sessionStorage,
                        persistentStorage,
                        apiClient,
                        supplementalInformationFormer);
        final AutoAuthenticationController autoAuthenticationController =
                new AutoAuthenticationController(
                        request, systemUpdater, kbcAuthenticator, kbcAuthenticator);
        manualOrAutoAuthAuthenticator = autoAuthenticationController;
        authenticator = autoAuthenticationController;
    }

    protected void configureHttpClient(TinkHttpClient client) {
        httpFilter = new KbcHttpFilter();
        client.addFilter(httpFilter);
        client.setUserAgent(KbcConstants.Headers.USER_AGENT_VALUE);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return authenticator;
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
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return transferDestinationRefreshController.fetchTransferDestinations(accounts);
    }

    private TransferDestinationRefreshController constructTransferDestinationRefreshController() {
        return new TransferDestinationRefreshController(
                metricRefreshController,
                new KbcTransferDestinationFetcher(apiClient, kbcLanguage, sessionStorage));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new KbcSessionHandler(httpFilter, apiClient, sessionStorage);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.of(
                new TransferController(
                        null,
                        new KbcBankTransferExecutor(
                                credentials,
                                persistentStorage,
                                sessionStorage,
                                apiClient,
                                catalog,
                                supplementalInformationHelper),
                        null,
                        null));
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
        return ProgressiveAuthController.of(
                        (ProgressiveAuthenticator) getAuthenticator(), credentials)
                .login(request);
    }

    @Override
    public boolean isManualAuthentication(Credentials credentials) {
        return manualOrAutoAuthAuthenticator.isManualAuthentication(credentials);
    }
}
