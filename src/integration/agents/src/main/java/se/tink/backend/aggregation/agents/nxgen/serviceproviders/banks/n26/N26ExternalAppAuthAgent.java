package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator.N26AppAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator.N26PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.passwordandapp.PasswordExternalAppAuthenticationController;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, IDENTITY_DATA})
public final class N26ExternalAppAuthAgent extends N26Agent {

    @Inject
    public N26ExternalAppAuthAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        N26AppAuthenticator authenticator =
                new N26AppAuthenticator(n26APiClient, sessionStorage, persistentStorage);
        N26PasswordAuthenticator passwordAuthenticator =
                new N26PasswordAuthenticator(n26APiClient, sessionStorage);
        return new PasswordExternalAppAuthenticationController(
                passwordAuthenticator, authenticator, supplementalInformationHelper);
    }
}
