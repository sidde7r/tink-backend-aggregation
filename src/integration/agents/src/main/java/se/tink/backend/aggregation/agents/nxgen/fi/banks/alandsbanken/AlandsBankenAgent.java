package se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator.CrossKeyAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator.CrossKeyKeyCardAuthenticator;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardAuthenticationController;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class AlandsBankenAgent extends CrossKeyAgent {

    public AlandsBankenAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, new AlandsBankenFIConfiguration());
    }

    @Override
    protected Authenticator constructAuthenticator() {

        return new AutoAuthenticationController(request, systemUpdater,
            new KeyCardAuthenticationController(catalog,
                supplementalInformationHelper,
                new CrossKeyKeyCardAuthenticator(apiClient, agentConfiguration, agentPersistentStorage, credentials),
                CrossKeyConstants.MultiFactorAuthentication.KEYCARD_PIN_LENGTH),
            new CrossKeyAutoAuthenticator(this.apiClient, agentPersistentStorage, this.credentials));
    }
}
