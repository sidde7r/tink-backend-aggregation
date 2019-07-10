package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank;

import java.util.Base64;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.authenticator.VolksbankAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.configuration.VolksbankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.fetcher.transactionalaccount.VolksbankTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.fetcher.transactionalaccount.VolksbankTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.session.VolksbankSessionHandler;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class VolksbankAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final VolksbankApiClient volksbankApiClient;
    private final VolksbankHttpClient httpClient;
    private final VolksbankUtils utils;
    private final String BANK_PATH;
    private final String redirectUrl;

    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public VolksbankAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        BANK_PATH = request.getProvider().getPayload().split(" ")[1];
        redirectUrl = request.getProvider().getPayload().split(" ")[2];

        this.httpClient = new VolksbankHttpClient(client, "certificate");
        this.utils = new VolksbankUtils(BANK_PATH);

        volksbankApiClient = new VolksbankApiClient(httpClient, sessionStorage, utils);

        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
    }

    @Override
    public void setConfiguration(final AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        final VolksbankConfiguration volksbankConfiguration =
                configuration
                        .getIntegrations()
                        .getClientConfiguration(
                                VolksbankConstants.Market.INTEGRATION_NAME,
                                VolksbankConstants.Market.CLIENT_NAME,
                                VolksbankConfiguration.class)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Volksbank configuration missing."));

        volksbankApiClient.setConfiguration(volksbankConfiguration);

        httpClient.setSslClientCertificate(
                Base64.getDecoder()
                        .decode(
                                volksbankConfiguration
                                        .getAisConfiguration()
                                        .getClientCertificateContent()),
                volksbankConfiguration.getAisConfiguration().getClientCertificatePass());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        VolksbankAuthenticator authenticator =
                new VolksbankAuthenticator(volksbankApiClient, sessionStorage, redirectUrl, utils);

        OAuth2AuthenticationController oAuth2AuthenticationController =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        authenticator,
                        credentials);
        return new AutoAuthenticationController(
                request,
                context,
                new ThirdPartyAppAuthenticationController<>(
                        oAuth2AuthenticationController, supplementalInformationHelper),
                oAuth2AuthenticationController);
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

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new VolksbankTransactionalAccountFetcher(volksbankApiClient),
                new TransactionFetcherController<>(
                        this.transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(
                                new VolksbankTransactionFetcher(volksbankApiClient))));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new VolksbankSessionHandler();
    }
}
