package se.tink.backend.aggregation.agents.nxgen.it.banks.ing;

import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.login.LoginProcess;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration.RegistrationProcess;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.scaffold.UserInteractionMultiStepsProcess;
import se.tink.backend.aggregation.agents.progressive.ProgressiveAuthAgent;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.SubsequentGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.ProductionAgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.SteppableAuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.SteppableAuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.scaffold.ModuleDependenciesRegistration;
import se.tink.backend.aggregation.nxgen.scaffold.ModuleDependenciesRegistry;
import se.tink.libraries.credentials.service.CredentialsRequest;

@AgentCapabilities(generateFromImplementedExecutors = true)
public final class IngAgent extends SubsequentGenerationAgent<IngAuthenticator>
        implements ProgressiveAuthAgent {

    private RegistrationProcess registrationProcess;

    private LoginProcess loginProcess;
    private IngAuthenticator ingAuthenticator;

    public IngAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(ProductionAgentComponentProvider.create(request, context, signatureKeyPair));
        initializeAgentDependencies(new IngModuleDependenciesRegistration());
    }

    public IngAgent(
            ModuleDependenciesRegistration moduleDependenciesRegistration,
            CredentialsRequest request,
            AgentContext context,
            SignatureKeyPair signatureKeyPair) {
        super(ProductionAgentComponentProvider.create(request, context, signatureKeyPair));
        initializeAgentDependencies(moduleDependenciesRegistration);
    }

    private void initializeAgentDependencies(
            ModuleDependenciesRegistration moduleDependenciesRegistration) {
        configureHttpClient();
        moduleDependenciesRegistration.registerExternalDependencies(
                client, sessionStorage, persistentStorage);
        moduleDependenciesRegistration.registerInternalModuleDependencies();
        ModuleDependenciesRegistry moduleDependenciesRegistry =
                moduleDependenciesRegistration.createModuleDependenciesRegistry();
        this.ingAuthenticator = moduleDependenciesRegistry.getBean(IngAuthenticator.class);
        this.registrationProcess = moduleDependenciesRegistry.getBean(RegistrationProcess.class);
        this.loginProcess = moduleDependenciesRegistry.getBean(LoginProcess.class);
        // Agent should have lifecycle methods and below methods should be invoked there
        registrationProcess.registerSteps();
        loginProcess.registerSteps();
    }

    private void configureHttpClient() {
        client.addMessageReader(new BodyReader());
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public IngAuthenticator getAuthenticator() {
        return ingAuthenticator;
    }

    @Override
    public SteppableAuthenticationResponse login(SteppableAuthenticationRequest request)
            throws Exception {
        UserInteractionMultiStepsProcess process =
                determineAuthenticationProcess(userRegistered(request));
        return process.execute(request);
    }

    private UserInteractionMultiStepsProcess determineAuthenticationProcess(boolean isRegistered) {
        return isRegistered ? loginProcess : registrationProcess;
    }

    private boolean userRegistered(SteppableAuthenticationRequest request) {
        return false;
    }

    @Override
    public boolean login() throws Exception {
        throw new UnsupportedOperationException("login() not supported");
    }
}
