package se.tink.backend.aggregation.agents.nxgen.se.openbanking.handelsbanken;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseAccountConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.HandelsbankenBankidAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc.SessionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.configuration.HandelsbankenBaseConfiguration;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class HandelsbankenSEAgent extends HandelsbankenBaseAgent {

    private final HandelsbankenAccountConverter accountConverter;
    private final HandelsbankenBaseApiClient apiClient;
    private final String clientName;
    private HandelsbankenBaseConfiguration handelsbankenBaseConfiguration;


    public HandelsbankenSEAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        this.accountConverter = new HandelsbankenAccountConverter();
        apiClient = new HandelsbankenBaseApiClient(client, sessionStorage);
        clientName = request.getProvider().getPayload();
    }

    @Override
    protected HandelsbankenBaseAccountConverter getAccountConverter() {
        return accountConverter;
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        handelsbankenBaseConfiguration =
            configuration
                .getIntegrations()
                .getClientConfiguration(
                    HandelsbankenBaseConstants.INTEGRATION_NAME,
                    clientName,
                    HandelsbankenBaseConfiguration.class)
                .orElseThrow(
                    () ->
                        new IllegalStateException(
                            HandelsbankenBaseConstants.ExceptionMessages
                                .CONFIG_MISSING));

        apiClient.setConfiguration(handelsbankenBaseConfiguration);
        configureHttpClient(client);
    }

    private void configureHttpClient(TinkHttpClient client) {
        client.setEidasProxy(handelsbankenBaseConfiguration.getEidasUrl(), "Tink");
    }

    @Override
    protected Authenticator constructAuthenticator() {

        return new BankIdAuthenticationController<SessionResponse>(
            supplementalRequester,
            new HandelsbankenBankidAuthenticator(apiClient, sessionStorage));
    }


}
