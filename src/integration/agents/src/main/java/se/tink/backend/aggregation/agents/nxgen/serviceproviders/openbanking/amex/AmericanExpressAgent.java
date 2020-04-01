package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import java.time.Clock;
import lombok.Getter;
import org.assertj.core.util.VisibleForTesting;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.apiclient.AmexAccessTokenProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.apiclient.AmexApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.apiclient.AmexThirdPartyAppRequestParamsProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.configuration.AmexConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.macgenerator.AmexMacGenerator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.macgenerator.MacSignatureCreator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.transactionalaccount.AmexTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.transactionalaccount.AmexTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.transactionalaccount.converter.AmexTransactionalAccountConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.transactionalaccount.storage.HmacAccountIdStorage;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.SubsequentProgressiveGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.hmac.HmacMultiTokenFetcher;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.hmac.HmacMultiTokenStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.AccessCodeStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.AccessTokenFetchHelper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.AccessTokenFetcher;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.OAuth2BasedTokenAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.ThirdPartyAppCallbackProcessor;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.TokenLifeTime;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.steps.ThirdPartyAppAuthenticationStepCreator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.authentication.HmacToken;

public class AmericanExpressAgent extends SubsequentProgressiveGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final AmexApiClient amexApiClient;
    private final StrongAuthenticationState strongAuthenticationState;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    @VisibleForTesting @Getter private final HmacMultiTokenStorage hmacMultiTokenStorage;

    @Inject
    public AmericanExpressAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);

        final AmexConfiguration amexConfiguration = getAgentConfiguration();
        final MacSignatureCreator macSignatureCreator = new MacSignatureCreator();
        final Clock clock = Clock.systemDefaultZone();
        final AmexMacGenerator amexMacGenerator =
                new AmexMacGenerator(amexConfiguration, macSignatureCreator, clock);

        this.hmacMultiTokenStorage =
                new HmacMultiTokenStorage(this.persistentStorage, this.sessionStorage);

        this.amexApiClient =
                new AmexApiClient(
                        amexConfiguration, this.client, amexMacGenerator, new ObjectMapper());

        this.strongAuthenticationState = new StrongAuthenticationState(request.getAppUriId());

        this.transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    @Override
    public void setConfiguration(final AgentsServiceConfiguration configuration) {
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
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public StatelessProgressiveAuthenticator getAuthenticator() {
        final AmexAccessTokenProvider amexAccessTokenProvider =
                new AmexAccessTokenProvider(amexApiClient);
        final AmexThirdPartyAppRequestParamsProvider thirdPartyAppRequestParamsProvider =
                new AmexThirdPartyAppRequestParamsProvider(amexApiClient);
        final AccessTokenFetchHelper<HmacToken> accessTokenFetchHelper =
                new AccessTokenFetchHelper<>(
                        amexAccessTokenProvider,
                        credentials,
                        new TokenLifeTime(
                                AccessTokenFetchHelper.DEFAULT_TOKEN_LIFETIME,
                                AccessTokenFetchHelper.DEFAULT_TOKEN_LIFETIME_UNIT));

        final ThirdPartyAppCallbackProcessor thirdPartyAppCallbackProcessor =
                new ThirdPartyAppCallbackProcessor(thirdPartyAppRequestParamsProvider);
        final AccessCodeStorage accessCodeStorage = new AccessCodeStorage(sessionStorage);
        final ThirdPartyAppAuthenticationStepCreator thirdPartyAppAuthenticationStepCreator =
                new ThirdPartyAppAuthenticationStepCreator(
                        thirdPartyAppCallbackProcessor,
                        accessCodeStorage,
                        strongAuthenticationState);

        final AccessTokenFetcher accessTokenFetcher =
                new HmacMultiTokenFetcher(
                        accessTokenFetchHelper, hmacMultiTokenStorage, accessCodeStorage);

        return new OAuth2BasedTokenAuthenticator(
                accessTokenFetcher, thirdPartyAppAuthenticationStepCreator);
    }

    private AmexConfiguration getAgentConfiguration() {
        return getAgentConfigurationController().getAgentConfiguration(AmexConfiguration.class);
    }

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        final HmacAccountIdStorage hmacAccountIdStorage = new HmacAccountIdStorage(sessionStorage);

        final AmexTransactionalAccountConverter amexTransactionalAccountConverter =
                new AmexTransactionalAccountConverter();

        final AmexTransactionalAccountFetcher accountFetcher =
                new AmexTransactionalAccountFetcher(
                        amexApiClient,
                        hmacMultiTokenStorage,
                        hmacAccountIdStorage,
                        amexTransactionalAccountConverter);

        final AmexTransactionFetcher transactionFetcher =
                new AmexTransactionFetcher(
                        amexApiClient, hmacAccountIdStorage, amexTransactionalAccountConverter);

        return new TransactionalAccountRefreshController(
                metricRefreshController, updateController, accountFetcher, transactionFetcher);
    }
}
