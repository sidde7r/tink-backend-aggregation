package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.SebAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.configuration.SebConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.session.SEBSessionHandler;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.SebAccountsAndCardsConstants;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class SebAbstractAgent<C extends SebAbstractApiClient> extends NextGenerationAgent {

    protected C apiClient;

    protected SebAbstractAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    public void setConfiguration(final AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        final SebConfiguration sebConfiguration =
                configuration
                        .getIntegrations()
                        .getClientConfiguration(
                                SebAccountsAndCardsConstants.Market.INTEGRATION_NAME,
                                SebAccountsAndCardsConstants.Market.CLIENT_NAME,
                                SebConfiguration.class)
                        .orElseThrow(() -> new IllegalStateException("SEB configuration missing."));

        apiClient.setConfiguration(sebConfiguration);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        SebAuthenticator authenticator = new SebAuthenticator(apiClient, sessionStorage);
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
    protected SessionHandler constructSessionHandler() {
        return new SEBSessionHandler(apiClient, sessionStorage);
    }
}
