package se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.DeutscheBankMultifactorAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.configuration.DeutscheBankConfiguration;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class DeutscheBankBEAgent extends DeutscheBankAgent {

    public DeutscheBankBEAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration configuration) {
        super(request, context, configuration);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        DeutscheBankConfiguration deutscheBankConfiguration =
                getAgentConfigurationController()
                        .getAgentConfiguration(DeutscheBankConfiguration.class);
        DeutscheBankBEApiClient apiClient =
                new DeutscheBankBEApiClient(client, sessionStorage, deutscheBankConfiguration);

        final DeutscheBankMultifactorAuthenticator deutscheBankAuthenticatorController =
                new DeutscheBankMultifactorAuthenticator(
                        apiClient,
                        sessionStorage,
                        credentials.getField(DeutscheBankConstants.CredentialKeys.IBAN),
                        credentials.getField(DeutscheBankConstants.CredentialKeys.PSU_ID),
                        strongAuthenticationState);

        return new AutoAuthenticationController(
                request,
                context,
                deutscheBankAuthenticatorController,
                deutscheBankAuthenticatorController);
    }
}
