package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import lombok.SneakyThrows;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.authenticator.SpardaAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.authenticator.SpardaRedirectHelper;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.client.SpardaAuthApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.client.SpardaErrorHandler;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.client.SpardaFetcherApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.client.SpardaRequestBuilder;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.client.SpardaTokenApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.fetcher.SpardaAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.fetcher.SpardaAccountMapper;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.fetcher.SpardaTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.fetcher.SpardaTransactionMapper;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

@AgentCapabilities({
    CHECKING_ACCOUNTS,
    SAVINGS_ACCOUNTS,
})
public class SpardaAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {
    private final SpardaAuthApiClient authApiClient;
    private final SpardaTokenApiClient tokenApiClient;
    private final SpardaFetcherApiClient fetcherApiClient;
    private final LocalDateTimeSource localDateTimeSource;
    private final RandomValueGenerator randomValueGenerator;
    private final SpardaStorage storage;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    @Inject
    public SpardaAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);

        localDateTimeSource = componentProvider.getLocalDateTimeSource();
        randomValueGenerator = componentProvider.getRandomValueGenerator();

        storage = new SpardaStorage(persistentStorage, sessionStorage);
        SpardaRequestBuilder requestBuilder =
                new SpardaRequestBuilder(
                        client,
                        randomValueGenerator,
                        request.getUserAvailability().getOriginatingUserIpOrDefault(),
                        storage);

        String bicCode = getBicCode();
        this.authApiClient = new SpardaAuthApiClient(requestBuilder, bicCode);

        this.tokenApiClient = new SpardaTokenApiClient(requestBuilder);

        SpardaErrorHandler errorHandler =
                new SpardaErrorHandler(tokenApiClient, storage, clientId());
        this.fetcherApiClient = new SpardaFetcherApiClient(requestBuilder, errorHandler);

        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
    }

    private String getBicCode() {
        if (SpardaConstants.SANDBOX) {
            return "TEST7999";
        } else if (provider.getPayload() != null) {
            return provider.getPayload();
        } else {
            // One provider of this agent supports multiple bics. We are using credential field to
            // gather and pass this info into the agent.
            return credentials.getField("bic-select");
        }
    }

    protected TransactionalAccountRefreshController
            constructTransactionalAccountRefreshController() {
        SpardaAccountFetcher accountFetcher =
                new SpardaAccountFetcher(fetcherApiClient, new SpardaAccountMapper());
        SpardaTransactionFetcher transactionFetcher =
                new SpardaTransactionFetcher(
                        fetcherApiClient,
                        new SpardaTransactionMapper(),
                        transactionPaginationHelper,
                        localDateTimeSource);

        return new TransactionalAccountRefreshController(
                metricRefreshController, updateController, accountFetcher, transactionFetcher);
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        client.setEidasProxy(configuration.getEidasProxy());
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
    protected Authenticator constructAuthenticator() {
        String redirectUrl =
                getAgentConfigurationController()
                        .getAgentConfiguration(SpardaConfiguration.class)
                        .getRedirectUrl();

        SpardaRedirectHelper helper =
                new SpardaRedirectHelper(
                        storage,
                        authApiClient,
                        tokenApiClient,
                        clientId(),
                        redirectUrl,
                        strongAuthenticationState,
                        localDateTimeSource,
                        randomValueGenerator);

        SpardaAuthenticator authenticator =
                new SpardaAuthenticator(
                        authApiClient,
                        storage,
                        new OAuth2AuthenticationController(
                                persistentStorage,
                                supplementalInformationHelper,
                                helper,
                                credentials,
                                strongAuthenticationState),
                        credentials);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        authenticator, supplementalInformationHelper),
                authenticator);
    }

    @SneakyThrows
    private String clientId() {
        AgentConfiguration<SpardaConfiguration> agentConfiguration =
                getAgentConfigurationController().getAgentConfiguration(SpardaConfiguration.class);
        return CertificateUtils.getOrganizationIdentifier(agentConfiguration.getQwac());
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }
}
