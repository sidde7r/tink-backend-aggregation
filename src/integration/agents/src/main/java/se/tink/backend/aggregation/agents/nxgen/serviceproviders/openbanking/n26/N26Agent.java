package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.TRANSFERS;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import java.util.List;
import java.util.Optional;
import lombok.SneakyThrows;
import se.tink.agent.sdk.operation.User;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentPisCapability;
import se.tink.backend.aggregation.agents.agentcapabilities.PisCapability;
import se.tink.backend.aggregation.agents.agentplatform.AgentPlatformHttpClient;
import se.tink.backend.aggregation.agents.agentplatform.authentication.AgentPlatformAgent;
import se.tink.backend.aggregation.agents.agentplatform.authentication.storage.RedirectTokensAccessor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.N26Constants.Url;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.N26OAuth2AuthenticationConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.fetch_consent.N26ConsentAccessor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.error.N26BankSiteErrorDiscoverer;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.error.N26BankSiteErrorHandler;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.executor.payment.N26OauthPaymentAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.executor.payment.N26Xs2aPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.fetcher.N26DevelopersTransactionDateFromFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.xs2a.N26Xs2aApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.xs2a.N26Xs2aAuthenticationDataAccessor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersForAgentPlatformApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.Xs2aAuthenticationDataAccessor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.configuration.Xs2aDevelopersConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.Xs2aDevelopersTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.utils.transfer.InferredTransferDestinations;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcess;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.logmasker.LogMasker;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionKeyWithInitDateFromFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.client.LoggingStrategy;
import se.tink.backend.aggregation.nxgen.http.filter.filters.RateLimitFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TerminatedHandshakeRetryFilter;
import se.tink.libraries.account.enums.AccountIdentifierType;

@AgentCapabilities({CHECKING_ACCOUNTS, TRANSFERS})
@AgentPisCapability(
        capabilities = {PisCapability.SEPA_CREDIT_TRANSFER},
        markets = {"DE", "ES", "IT", "FR"})
public final class N26Agent extends AgentPlatformAgent
        implements RefreshCheckingAccountsExecutor, RefreshTransferDestinationExecutor {

    private final Xs2aAuthenticationDataAccessor xs2aAuthenticationDataAccessor;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final Xs2aDevelopersForAgentPlatformApiClient xs2aApiClient;
    private final ObjectMapper objectMapper;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final LogMasker logMasker;

    @Inject
    public N26Agent(AgentComponentProvider componentProvider) {
        super(componentProvider);

        setJsonHttpTrafficLogsEnabled(true);
        client.setLoggingStrategy(LoggingStrategy.EXPERIMENTAL);

        logMasker = componentProvider.getContext().getLogMasker();
        objectMapper = new ObjectMapper();
        RedirectTokensAccessor oAuth2TokenAccessor =
                new RedirectTokensAccessor(persistentStorage, objectMapper);
        xs2aAuthenticationDataAccessor =
                constructXs2aAuthenticationDataAccessor(
                        oAuth2TokenAccessor,
                        new N26ConsentAccessor(
                                new AgentAuthenticationPersistedData(persistentStorage),
                                objectMapper));
        xs2aApiClient = constructXs2aApiClient(componentProvider);

        transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController(
                        componentProvider, oAuth2TokenAccessor);
        this.supplementalInformationHelper = componentProvider.getSupplementalInformationHelper();
        client.setResponseStatusHandler(
                new N26BankSiteErrorHandler(new N26BankSiteErrorDiscoverer()));
        client.addFilter(new RateLimitFilter(provider.getName(), 500, 1500, 3));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public AgentAuthenticationProcess getAuthenticationProcess() {
        return new N26OAuth2AuthenticationConfig(
                        new AgentPlatformHttpClient(client),
                        objectMapper,
                        getN26AgentConfiguration(),
                        catalog,
                        logMasker)
                .authenticationProcess();
    }

    @Override
    public boolean isBackgroundRefreshPossible() {
        return true;
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
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        client.setEidasProxy(configuration.getEidasProxy());
        client.setFollowRedirects(false);
        client.addFilter(new TerminatedHandshakeRetryFilter());
    }

    @SneakyThrows
    private N26AgentConfiguration getN26AgentConfiguration() {
        AgentConfiguration<Xs2aDevelopersConfiguration> agentConfiguration =
                getAgentConfigurationController()
                        .getAgentConfiguration(Xs2aDevelopersConfiguration.class);
        String clientId = CertificateUtils.getOrganizationIdentifier(agentConfiguration.getQwac());

        String redirectUrl = agentConfiguration.getRedirectUrl();
        return new N26AgentConfiguration(clientId, Url.BASE_URL, redirectUrl);
    }

    private Xs2aDevelopersForAgentPlatformApiClient constructXs2aApiClient(
            AgentComponentProvider componentProvider) {
        User user = componentProvider.getUser();

        return new N26Xs2aApiClient(
                client,
                persistentStorage,
                getN26AgentConfiguration(),
                user.isPresent(),
                user.getIpAddress(),
                componentProvider.getRandomValueGenerator(),
                xs2aAuthenticationDataAccessor,
                logMasker);
    }

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController(
            AgentComponentProvider componentProvider, RedirectTokensAccessor oAuth2TokenAccessor) {

        final Xs2aDevelopersTransactionalAccountFetcher accountFetcher =
                new Xs2aDevelopersTransactionalAccountFetcher(xs2aApiClient, oAuth2TokenAccessor);

        final TransactionFetcher<TransactionalAccount> transactionFetcher =
                new TransactionKeyWithInitDateFromFetcherController<>(
                        request,
                        new N26DevelopersTransactionDateFromFetcher<TransactionalAccount>(
                                xs2aApiClient,
                                componentProvider.getLocalDateTimeSource(),
                                componentProvider.getUser().isPresent()));

        return new TransactionalAccountRefreshController(
                metricRefreshController, updateController, accountFetcher, transactionFetcher);
    }

    private Xs2aAuthenticationDataAccessor constructXs2aAuthenticationDataAccessor(
            RedirectTokensAccessor oAuth2TokenAccessor, N26ConsentAccessor n26ConsentAccessor) {
        return new N26Xs2aAuthenticationDataAccessor(oAuth2TokenAccessor, n26ConsentAccessor);
    }

    private OAuth2AuthenticationController constructOAuth2AuthenticationController() {
        return new OAuth2AuthenticationController(
                persistentStorage,
                supplementalInformationHelper,
                new N26OauthPaymentAuthenticator(
                        new AgentPlatformHttpClient(client),
                        getN26AgentConfiguration(),
                        request,
                        objectMapper,
                        logMasker),
                credentials,
                strongAuthenticationState);
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {

        N26Xs2aPaymentExecutor n26Xs2aPaymentExecutor =
                new N26Xs2aPaymentExecutor(
                        xs2aApiClient,
                        new ThirdPartyAppAuthenticationController<>(
                                constructOAuth2AuthenticationController(),
                                supplementalInformationHelper),
                        credentials,
                        persistentStorage);

        return Optional.of(new PaymentController(n26Xs2aPaymentExecutor, n26Xs2aPaymentExecutor));
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return InferredTransferDestinations.forPaymentAccounts(
                accounts, AccountIdentifierType.IBAN);
    }
}
