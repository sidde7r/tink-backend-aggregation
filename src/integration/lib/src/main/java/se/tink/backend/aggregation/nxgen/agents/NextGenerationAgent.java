package se.tink.backend.aggregation.nxgen.agents;

import java.util.Optional;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.ProductionAgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.ProviderSessionCacheController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.instrumentation.FetcherInstrumentationRegistry;
import se.tink.libraries.credentials.service.CredentialsRequest;

public abstract class NextGenerationAgent extends SubsequentGenerationAgent<Authenticator> {

    protected final SupplementalInformationHelper supplementalInformationHelper;
    protected final SupplementalInformationController supplementalInformationController;
    protected final ProviderSessionCacheController providerSessionCacheController;
    private Authenticator authenticator;

    protected NextGenerationAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        this.supplementalInformationController =
                componentProvider.getSupplementalInformationController();
        this.supplementalInformationHelper = componentProvider.getSupplementalInformationHelper();
        this.providerSessionCacheController =
                new ProviderSessionCacheController(providerSessionCacheContext);
    }

    /** @deprecated Use AgentComponentProvider constructor instead */
    @Deprecated
    protected NextGenerationAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        this(ProductionAgentComponentProvider.create(request, context, signatureKeyPair));
    }

    protected abstract Authenticator constructAuthenticator();

    public Authenticator getAuthenticator() {
        if (authenticator == null) {
            authenticator = this.constructAuthenticator();
        }
        return authenticator;
    }

    @Override
    public boolean login() throws AuthenticationException, AuthorizationException {
        getAuthenticator().authenticate(credentials);
        return true;
    }

    public Optional<FetcherInstrumentationRegistry> getFetcherInstrumentation() {
        return Optional.empty();
    }
}
