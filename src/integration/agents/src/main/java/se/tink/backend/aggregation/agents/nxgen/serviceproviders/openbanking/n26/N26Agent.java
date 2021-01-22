package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import lombok.SneakyThrows;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentplatform.AgentPlatformHttpClient;
import se.tink.backend.aggregation.agents.agentplatform.authentication.AgentPlatformAgent;
import se.tink.backend.aggregation.agents.agentplatform.authentication.storage.RedirectTokensAccessor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.N26Constants.Url;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.N26OAuth2AuthenticationConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.fetch_consent.N26ConsentAccessor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.fetcher.N26DevelopersTransactionDateFromFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.xs2a.N26Xs2aApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.xs2a.N26Xs2aAuthenticationDataAccessor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersForAgentPlatformApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.Xs2aAuthenticationDataAccessor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.configuration.Xs2aDevelopersConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.Xs2aDevelopersTransactionalAccountFetcher;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcess;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionKeyWithInitDateFromFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@AgentCapabilities({CHECKING_ACCOUNTS})
public final class N26Agent extends AgentPlatformAgent implements RefreshCheckingAccountsExecutor {

    private final Xs2aAuthenticationDataAccessor xs2aAuthenticationDataAccessor;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final Xs2aDevelopersForAgentPlatformApiClient xs2aApiClient;
    private final RedirectTokensAccessor oAuth2TokenAccessor;
    private final ObjectMapper objectMapper;

    @Inject
    public N26Agent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        objectMapper = new ObjectMapper();
        oAuth2TokenAccessor = new RedirectTokensAccessor(persistentStorage, objectMapper);
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
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public AgentAuthenticationProcess getAuthenticationProcess() {
        client.setEidasProxy(configuration.getEidasProxy());
        client.setFollowRedirects(false);
        return new N26OAuth2AuthenticationConfig(
                        new AgentPlatformHttpClient(client),
                        objectMapper,
                        getN26AgentConfiguration(),
                        catalog)
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

    @SneakyThrows
    private N26AgentConfiguration getN26AgentConfiguration() {
        AgentConfiguration<Xs2aDevelopersConfiguration> agentConfiguration =
                getAgentConfigurationController()
                        .getAgentConfiguration(Xs2aDevelopersConfiguration.class);
        String organizationIdentifier =
                CertificateUtils.getOrganizationIdentifier(agentConfiguration.getQwac());

        String redirectUrl = agentConfiguration.getRedirectUrl();
        return new N26AgentConfiguration(organizationIdentifier, Url.BASE_URL, redirectUrl);
    }

    private Xs2aDevelopersForAgentPlatformApiClient constructXs2aApiClient(
            AgentComponentProvider componentProvider) {
        return new N26Xs2aApiClient(
                client,
                persistentStorage,
                getN26AgentConfiguration(),
                request.isManual(),
                userIp,
                componentProvider.getRandomValueGenerator(),
                xs2aAuthenticationDataAccessor);
    }

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController(
            AgentComponentProvider componentProvider, RedirectTokensAccessor oAuth2TokenAccessor) {

        final Xs2aDevelopersTransactionalAccountFetcher accountFetcher =
                new Xs2aDevelopersTransactionalAccountFetcher(xs2aApiClient, oAuth2TokenAccessor);

        final TransactionFetcher<TransactionalAccount> transactionFetcher =
                new TransactionKeyWithInitDateFromFetcherController<>(
                        request,
                        new N26DevelopersTransactionDateFromFetcher<TransactionalAccount>(
                                xs2aApiClient, componentProvider.getLocalDateTimeSource()));

        return new TransactionalAccountRefreshController(
                metricRefreshController, updateController, accountFetcher, transactionFetcher);
    }

    private Xs2aAuthenticationDataAccessor constructXs2aAuthenticationDataAccessor(
            RedirectTokensAccessor oAuth2TokenAccessor, N26ConsentAccessor n26ConsentAccessor) {
        return new N26Xs2aAuthenticationDataAccessor(oAuth2TokenAccessor, n26ConsentAccessor);
    }
}
