package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama;

import java.util.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.authenticator.BoursoramaAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.client.BoursoramaApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.client.BoursoramaMessageSignFilter;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.client.BoursoramaSignatureHeaderGenerator;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.configuration.BoursoramaConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity.IdentityEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.fetcher.BoursoramaTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.payment.BoursoramaPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.FrOpenBankingPaymentExecutor;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.agentcontext.AgentContextProviderImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.GeneratedValueProviderImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ActualLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGeneratorImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation.SupplementalInformationProviderImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient.LegacyTinkHttpClientProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.identitydata.IdentityData;

public class BoursoramaAgent extends NextGenerationAgent
        implements RefreshIdentityDataExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor {

    private final BoursoramaApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final BoursoramaAuthenticator authenticator;

    public BoursoramaAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration agentsServiceConfiguration) {
        super(
                new AgentComponentProvider(
                        new LegacyTinkHttpClientProvider(
                                request, context, agentsServiceConfiguration.getSignatureKeyPair()),
                        new SupplementalInformationProviderImpl(context, request),
                        new AgentContextProviderImpl(request, context),
                        new GeneratedValueProviderImpl(
                                new ActualLocalDateTimeSource(), new RandomValueGeneratorImpl())));

        BoursoramaConfiguration agentConfiguration = getAgentConfiguration();

        this.apiClient = constructApiClient(agentConfiguration, agentsServiceConfiguration);
        this.authenticator = new BoursoramaAuthenticator(apiClient, sessionStorage);
        this.transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    private BoursoramaConfiguration getAgentConfiguration() {
        BoursoramaConfiguration configuration =
                getAgentConfigurationController()
                        .getAgentConfiguration(BoursoramaConfiguration.class);

        Objects.requireNonNull(configuration.getBaseUrl());
        Objects.requireNonNull(configuration.getClientId());
        Objects.requireNonNull(configuration.getQsealKeyUrl());
        Objects.requireNonNull(configuration.getRedirectUrl());

        return configuration;
    }

    private BoursoramaApiClient constructApiClient(
            BoursoramaConfiguration agentConfiguration,
            AgentsServiceConfiguration agentsServiceConfiguration) {

        BoursoramaMessageSignFilter messageSignFilter =
                constructMessageSignFilter(agentsServiceConfiguration, agentConfiguration);
        client.addFilter(messageSignFilter);
        return new BoursoramaApiClient(client, agentConfiguration, sessionStorage);
    }

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        BoursoramaTransactionalAccountFetcher accountFetcher =
                new BoursoramaTransactionalAccountFetcher(apiClient, sessionStorage);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(accountFetcher)));
    }

    private BoursoramaMessageSignFilter constructMessageSignFilter(
            AgentsServiceConfiguration agentsServiceConfiguration,
            BoursoramaConfiguration agentConfiguration) {
        return new BoursoramaMessageSignFilter(
                new BoursoramaSignatureHeaderGenerator(
                        agentsServiceConfiguration.getEidasProxy(),
                        getEidasIdentity(),
                        agentConfiguration.getQsealKeyUrl()));
    }

    @Override
    protected Authenticator constructAuthenticator() {

        final OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        authenticator,
                        credentials,
                        strongAuthenticationState);

        return new AutoAuthenticationController(
                request,
                context,
                new ThirdPartyAppAuthenticationController<>(
                        controller, supplementalInformationHelper),
                controller);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        String accessToken = sessionStorage.get(BoursoramaConstants.USER_HASH);
        IdentityEntity identityEntity = apiClient.fetchIdentityData(accessToken);

        return new FetchIdentityDataResponse(
                IdentityData.builder()
                        .addFirstNameElement(null)
                        .addSurnameElement(identityEntity.getConnectedPsu())
                        .setDateOfBirth(null)
                        .build());
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
    public Optional<PaymentController> constructPaymentController() {
        BoursoramaConfiguration configuration =
                getAgentConfigurationController()
                        .getAgentConfiguration(BoursoramaConfiguration.class);

        FrOpenBankingPaymentExecutor paymentExecutor =
                new FrOpenBankingPaymentExecutor(
                        new BoursoramaPaymentApiClient(client, configuration),
                        configuration.getRedirectUrl(),
                        sessionStorage,
                        strongAuthenticationState,
                        supplementalInformationHelper);

        return Optional.of(new PaymentController(paymentExecutor, paymentExecutor));
    }
}
