package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45;

import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.authenticator.AmericanExpressPasswordAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.fetcher.AmericanExpressCreditCardAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.fetcher.AmericanExpressTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.session.AmericanExpressSessionHandler;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.MultiIpGateway;
import se.tink.libraries.strings.StringUtils;

public abstract class AmericanExpressAgent extends NextGenerationAgent
        implements RefreshCreditCardAccountsExecutor {

    private final AmericanExpressApiClient apiClient;
    private final AmericanExpressConfiguration config;
    private final MultiIpGateway gateway;
    private final CreditCardRefreshController creditCardRefreshController;

    protected AmericanExpressAgent(
            AgentComponentProvider componentProvider, AmericanExpressConfiguration config) {
        super(componentProvider);
        generateDeviceId();
        this.apiClient = new AmericanExpressApiClient(client, sessionStorage, config);
        this.config = config;
        this.gateway = new MultiIpGateway(client, credentials.getUserId(), credentials.getId());

        this.creditCardRefreshController = constructCreditCardRefreshController();
    }

    private void generateDeviceId() {
        String uid = credentials.getField(Field.Key.USERNAME);
        String deviceId = StringUtils.hashAsUUID(uid);
        sessionStorage.put(AmericanExpressConstants.Tags.HARDWARE_ID, deviceId);
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        // Amex is throttling how many requests we can send per IP address.
        // Use this multiIp gateway to originate from different IP addresses.
        gateway.setMultiIpGateway(configuration.getIntegrations());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(
                new AmericanExpressPasswordAuthenticator(apiClient, config, sessionStorage));
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
        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                new AmericanExpressCreditCardAccountFetcher(sessionStorage, config),
                new AmericanExpressTransactionFetcher(apiClient, config));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new AmericanExpressSessionHandler(apiClient, sessionStorage);
    }
}
