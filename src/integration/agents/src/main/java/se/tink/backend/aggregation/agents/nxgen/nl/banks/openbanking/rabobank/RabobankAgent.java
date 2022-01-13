package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import java.security.cert.CertificateException;
import java.time.ZoneId;
import java.util.Date;
import se.tink.agent.sdk.operation.User;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.HttpClient;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.QueryParams;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.authenticator.RabobankAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.authenticator.RabobankAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.configuration.RabobankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.transactional.SandboxTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.transactional.TransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.transactional.TransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.filter.RabobankFailureFilter;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.filter.RabobankRetryFilter;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.filter.RabobankUserRefreshLimitExceededFilter;
import se.tink.backend.aggregation.agents.progressive.ProgressiveAuthAgent;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.module.QSealcSignerModuleRSASHA256;
import se.tink.backend.aggregation.nxgen.agents.SubsequentGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.progressive.AutoAuthenticationProgressiveController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationProgressiveController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.progressive.ThirdPartyAppAuthenticationProgressiveController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.ProgressiveAuthController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.SteppableAuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.SteppableAuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.DateLimitTransactionPaginatorHelperFactory;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelper;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.OAuth2TokenSessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.AccessExceededFilter;

@AgentDependencyModules(modules = QSealcSignerModuleRSASHA256.class)
@AgentCapabilities({CHECKING_ACCOUNTS})
public final class RabobankAgent
        extends SubsequentGenerationAgent<AutoAuthenticationProgressiveController>
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                ProgressiveAuthAgent {

    private final RabobankApiClient apiClient;
    private final String clientName;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final AutoAuthenticationProgressiveController progressiveAuthenticator;

    @Inject
    public RabobankAgent(AgentComponentProvider componentProvider, QsealcSigner qsealcSigner) {
        super(componentProvider);
        configureHttpClient(client);
        clientName = componentProvider.getProvider().getPayload();

        final AgentConfiguration<RabobankConfiguration> agentConfiguration =
                getAgentConfigurationController()
                        .getAgentConfiguration(RabobankConfiguration.class);

        final RabobankConfiguration rabobankConfiguration =
                agentConfiguration.getProviderSpecificConfiguration();
        final String password = rabobankConfiguration.getClientSSLKeyPassword();
        final byte[] p12 = rabobankConfiguration.getClientSSLP12bytes();
        final String qsealPem;
        try {
            qsealPem =
                    CertificateUtils.getDerEncodedCertFromBase64EncodedCertificate(
                            agentConfiguration.getQsealc());
        } catch (CertificateException e) {
            throw new IllegalStateException("Invalid qsealc detected");
        }

        client.setSslClientCertificate(p12, password);

        RabobankSignatureHeaderBuilder signatureHeaderBuilder =
                new RabobankSignatureHeaderBuilder(qsealPem, qsealcSigner);

        final User user = componentProvider.getUser();
        apiClient =
                new RabobankApiClient(
                        client,
                        persistentStorage,
                        rabobankConfiguration,
                        signatureHeaderBuilder,
                        user);

        RabobankConsentStatusValidator rabobankConsentStatusValidator =
                new RabobankConsentStatusValidator(
                        apiClient,
                        persistentStorage,
                        signatureHeaderBuilder,
                        rabobankConfiguration);

        transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController(
                        componentProvider.getLocalDateTimeSource(), user);

        final OAuth2AuthenticationProgressiveController controller =
                new RabobankAuthenticationController(
                        persistentStorage,
                        new RabobankAuthenticator(
                                apiClient,
                                persistentStorage,
                                agentConfiguration,
                                componentProvider,
                                rabobankConsentStatusValidator),
                        credentials,
                        strongAuthenticationState);

        progressiveAuthenticator =
                new AutoAuthenticationProgressiveController(
                        request,
                        context,
                        new ThirdPartyAppAuthenticationProgressiveController(controller),
                        controller);

        String refreshToken = persistentStorage.get(QueryParams.REFRESH_TOKEN);
        if (refreshToken != null) {
            ImmutableSet<String> whitelistedValues = ImmutableSet.of(refreshToken);
            context.getLogMasker().addAgentWhitelistedValues(whitelistedValues);
        }
    }

    private void configureHttpClient(TinkHttpClient client) {
        client.addFilter(new AccessExceededFilter());
        client.addFilter(
                new RabobankRetryFilter(
                        HttpClient.MAX_RETRIES, HttpClient.RETRY_SLEEP_MILLISECONDS));
        client.addFilter(new RabobankUserRefreshLimitExceededFilter());
        client.addFilter(new RabobankFailureFilter());
    }

    @Override
    public boolean login() {
        throw new AssertionError(); // ProgressiveAuthAgent::login should always be used
    }

    @Override
    public SteppableAuthenticationResponse login(final SteppableAuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        return ProgressiveAuthController.of(progressiveAuthenticator, credentials).login(request);
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
            LocalDateTimeSource localDateTimeSource, User user) {

        Date dateLimit =
                Date.from(
                        (localDateTimeSource
                                .now()
                                .minusYears(8)
                                .atZone(ZoneId.systemDefault())
                                .toInstant()));

        TransactionPaginationHelper paginationHelper =
                new DateLimitTransactionPaginatorHelperFactory().create(request, dateLimit);

        TransactionDatePaginator<TransactionalAccount> transactionFetcher =
                isSandbox()
                        ? new SandboxTransactionFetcher(apiClient)
                        : new TransactionFetcher(apiClient, dateLimit, user.isPresent());

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new TransactionalAccountFetcher(apiClient),
                new TransactionFetcherController<>(
                        paginationHelper,
                        new TransactionDatePaginationController.Builder<>(transactionFetcher)
                                .setLocalDateTimeSource(localDateTimeSource)
                                .build()));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new OAuth2TokenSessionHandler(persistentStorage);
    }

    @Override
    public AutoAuthenticationProgressiveController getAuthenticator() {
        return progressiveAuthenticator;
    }

    private boolean isSandbox() {
        return clientName.toLowerCase().contains("sandbox");
    }
}
