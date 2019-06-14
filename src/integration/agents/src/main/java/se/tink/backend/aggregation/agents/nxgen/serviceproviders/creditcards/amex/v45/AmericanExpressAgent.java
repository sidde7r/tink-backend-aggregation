package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45;

import java.util.Optional;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.authenticator.AmericanExpressPasswordAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.fetcher.AmericanExpressCreditCardAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.fetcher.AmericanExpressTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.session.AmericanExpressSessionHandler;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.MultiIpGateway;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.strings.StringUtils;

public class AmericanExpressAgent extends NextGenerationAgent {

    private final AmericanExpressApiClient apiClient;
    private final AmericanExpressConfiguration config;
    private final MultiIpGateway gateway;

    protected AmericanExpressAgent(
            CredentialsRequest request,
            AgentContext context,
            SignatureKeyPair signatureKeyPair,
            AmericanExpressConfiguration config) {
        super(request, context, signatureKeyPair);
        generateDeviceId();
        this.apiClient = new AmericanExpressApiClient(client, sessionStorage, config);
        this.config = config;
        this.gateway = new MultiIpGateway(client, credentials);
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
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        return Optional.of(
                new CreditCardRefreshController(
                        metricRefreshController,
                        updateController,
                        new AmericanExpressCreditCardAccountFetcher(sessionStorage, config),
                        new AmericanExpressTransactionFetcher(apiClient, config)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new AmericanExpressSessionHandler(apiClient, sessionStorage);
    }
}
