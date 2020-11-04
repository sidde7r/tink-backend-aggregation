package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;

import com.google.inject.Inject;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.authenticator.BelfiusAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.configuration.BelfiusConfiguration;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.executor.payment.BelfiusPaymentController;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.executor.payment.BelfiusPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount.BelfiusTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@Slf4j
@AgentCapabilities({CHECKING_ACCOUNTS})
public final class BelfiusAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final BelfiusApiClient apiClient;
    private final AgentConfiguration<BelfiusConfiguration> agentConfiguration;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final RandomValueGenerator randomValueGenerator;

    @Inject
    public BelfiusAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        this.agentConfiguration =
                getAgentConfigurationController().getAgentConfiguration(BelfiusConfiguration.class);
        this.randomValueGenerator = componentProvider.getRandomValueGenerator();
        this.apiClient = new BelfiusApiClient(client, agentConfiguration, randomValueGenerator);
        this.transactionalAccountRefreshController = getTransactionalAccountRefreshController();
        client.setResponseStatusHandler(new BelfiusResponseStatusHandler(persistentStorage));
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        client.setEidasProxy(configuration.getEidasProxy());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new BelfiusAuthenticator(
                                apiClient,
                                persistentStorage,
                                agentConfiguration,
                                credentials.getField(BelfiusConstants.CredentialKeys.IBAN)),
                        credentials,
                        strongAuthenticationState);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        controller, supplementalInformationHelper),
                controller);
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

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        final BelfiusTransactionalAccountFetcher accountFetcher =
                new BelfiusTransactionalAccountFetcher(apiClient, persistentStorage);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(accountFetcher)));
    }

    @Override
    public boolean login() throws AuthenticationException, AuthorizationException {
        logOauth2TokenDetails(false);
        boolean login = super.login();
        logOauth2TokenDetails(true);
        return login;
    }

    private void logOauth2TokenDetails(boolean after) {
        try {
            persistentStorage
                    .get(PersistentStorageKeys.OAUTH_2_TOKEN, OAuth2Token.class)
                    .ifPresent(token -> logToken(token, after));
        } catch (RuntimeException e) {
            log.error("error logging oauth 2 token details");
        }
    }

    private void logToken(OAuth2Token token, boolean after) {
        String accessTokenHash = generateHash(token.getAccessToken());
        String refreshTokenHash = token.getRefreshToken().map(this::generateHash).orElse("empty");
        log.info(
                "The OAUTH_2_TOKEN {} login has properties: accessTokenHash: {}, refreshTokenHash: {}, expiresInSeconds: {}, refreshExpiresInSeconds: {}, issuedAt: {}",
                after ? "after" : "before",
                accessTokenHash,
                refreshTokenHash,
                token.getExpiresInSeconds(),
                token.getRefreshExpiresInSeconds(),
                token.getIssuedAt());
    }

    private String generateHash(String input) {
        return Hash.sha256Base64(input.getBytes()).substring(0, 2);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        BelfiusPaymentExecutor paymentExecutor =
                new BelfiusPaymentExecutor(
                        apiClient, sessionStorage, configuration, getEidasIdentity());

        return Optional.of(
                new BelfiusPaymentController(
                        paymentExecutor,
                        supplementalInformationHelper,
                        sessionStorage,
                        strongAuthenticationState));
    }
}
