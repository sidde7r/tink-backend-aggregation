package se.tink.backend.aggregation.agents.nxgen.se.banks.alandsbanken.keycard;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.INVESTMENTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.LOANS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.se.banks.alandsbanken.AlandsBankenSeConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator.CrossKeyAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator.CrossKeyKeyCardAuthenticator;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardAuthenticationController;

@AgentCapabilities({
    CHECKING_ACCOUNTS,
    SAVINGS_ACCOUNTS,
    CREDIT_CARDS,
    INVESTMENTS,
    IDENTITY_DATA,
    LOANS
})
public final class AlandsBankenKeyCardSeAgent extends CrossKeyAgent {

    @Inject
    public AlandsBankenKeyCardSeAgent(
            AgentComponentProvider provider, SignatureKeyPair signatureKeyPair) {
        super(provider, signatureKeyPair, new AlandsBankenSeConfiguration());
    }

    @Override
    protected Authenticator constructAuthenticator() {

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new KeyCardAuthenticationController(
                        catalog,
                        supplementalInformationController,
                        new CrossKeyKeyCardAuthenticator(
                                apiClient, agentConfiguration, agentPersistentStorage, credentials),
                        CrossKeyConstants.MultiFactorAuthentication.KEYCARD_PIN_LENGTH),
                new CrossKeyAutoAuthenticator(
                        this.apiClient, agentPersistentStorage, this.credentials));
    }
}
