package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.INVESTMENTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.LOANS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.JyskeKeyCardAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataCryptoComponentsProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataCryptoComponentsProviderModule;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdIFrameControllerInitializer;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdIFrameControllerInitializerModule;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS, INVESTMENTS, LOANS})
@AgentDependencyModules(
        modules = {
            NemIdIFrameControllerInitializerModule.class,
            BankdataCryptoComponentsProviderModule.class
        })
public final class JyskeKeyCardAgent extends JyskeAbstractAgent {

    @Inject
    public JyskeKeyCardAgent(
            AgentComponentProvider agentComponentProvider,
            NemIdIFrameControllerInitializer iFrameControllerInitializer,
            BankdataCryptoComponentsProvider cryptoComponentsProvider) {
        super(agentComponentProvider, iFrameControllerInitializer, cryptoComponentsProvider);
    }

    @Override
    protected Authenticator constructAuthenticator() {

        JyskePersistentStorage jyskePersistentStorage =
                new JyskePersistentStorage(persistentStorage);
        JyskeKeyCardAuthenticator jyskeKeyCardAuthenticator =
                new JyskeKeyCardAuthenticator(apiClient, jyskePersistentStorage, credentials);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new KeyCardAuthenticationController(
                        catalog, supplementalInformationController, jyskeKeyCardAuthenticator),
                jyskeKeyCardAuthenticator);
    }
}
