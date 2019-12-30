package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.authenticator.BoursoramaAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.client.BoursoramaApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.client.BoursoramaAuthenticationFilter;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.client.BoursoramaMessageSignFilter;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.client.BoursoramaSignatureHeaderGenerator;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.configuration.BoursoramaConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity.IdentityEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.fetcher.BoursoramaTransactionalAccountFetcher;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
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
    private final BoursoramaSignatureHeaderGenerator boursoramaSignatureHeaderGenerator;
    private BoursoramaAuthenticator authenticator;

    public BoursoramaAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, true);

        this.boursoramaSignatureHeaderGenerator = new BoursoramaSignatureHeaderGenerator();
        this.apiClient = new BoursoramaApiClient(client);
        client.addFilter(new BoursoramaMessageSignFilter(boursoramaSignatureHeaderGenerator));
        BoursoramaAuthenticationFilter authenticationFilter = new BoursoramaAuthenticationFilter();
        client.addFilter(authenticationFilter);

        authenticator =
                new BoursoramaAuthenticator(apiClient, sessionStorage, authenticationFilter);
        BoursoramaTransactionalAccountFetcher accountFetcher =
                new BoursoramaTransactionalAccountFetcher(apiClient, sessionStorage);
        this.transactionalAccountRefreshController =
                new TransactionalAccountRefreshController(
                        metricRefreshController, updateController, accountFetcher, accountFetcher);
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
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        BoursoramaConfiguration agentConfiguration =
                getAgentConfigurationController()
                        .getAgentConfiguration(BoursoramaConfiguration.class);

        authenticator.setConfiguration(agentConfiguration);
        apiClient.setConfiguration(agentConfiguration);

        boursoramaSignatureHeaderGenerator.setConfiguration(
                configuration.getEidasProxy(),
                getEidasIdentity(),
                agentConfiguration.getQsealKeyUrl());
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
}
