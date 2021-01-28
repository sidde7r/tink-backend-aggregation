package se.tink.backend.aggregation.agents.nxgen.hu.openbanking.unicredit;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.unicredit.authenticator.UnicreditAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.unicredit.authenticator.UnicreditAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.configuration.UnicreditProviderConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;

@AgentCapabilities(generateFromImplementedExecutors = true)
public final class UnicreditAgent extends UnicreditBaseAgent {

    private static final UnicreditProviderConfiguration PROVIDER_CONFIG =
            new UnicreditProviderConfiguration("NOT_YET_DEVELOPED", "NOT_YET_DEVELOPED");

    @Inject
    public UnicreditAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, PROVIDER_CONFIG);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        // TODO redirect back doesn't work on sandbox
        //      If works on prod custom controller not necessary
        final UnicreditAuthenticationController controller =
                new UnicreditAuthenticationController(
                        new UnicreditAuthenticator(unicreditStorage, apiClient),
                        strongAuthenticationState);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        controller, supplementalInformationHelper),
                controller);
    }
}
