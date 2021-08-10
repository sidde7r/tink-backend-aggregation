package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.INVESTMENTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.LOANS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.JyskeNemidAuthenticator;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdIFrameControllerInitializer;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdIFrameControllerInitializerModule;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.NemIdCodeAppAuthenticationController;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS, INVESTMENTS, LOANS})
@AgentDependencyModules(modules = {NemIdIFrameControllerInitializerModule.class})
public final class JyskeNemidAgent extends JyskeAbstractAgent {

    @Inject
    public JyskeNemidAgent(
            AgentComponentProvider agentComponentProvider,
            NemIdIFrameControllerInitializer iFrameControllerInitializer) {
        super(agentComponentProvider, iFrameControllerInitializer);
    }

    @Override
    protected Authenticator constructAuthenticator() {

        String username = credentials.getField(Field.Key.USERNAME);
        String password = credentials.getField(Field.Key.PASSWORD);

        JyskeNemidAuthenticator jyskeNemidAuthenticator =
                new JyskeNemidAuthenticator(
                        apiClient, client, persistentStorage, username, password);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new NemIdCodeAppAuthenticationController(
                        jyskeNemidAuthenticator, supplementalInformationController, catalog),
                jyskeNemidAuthenticator);
    }
}
