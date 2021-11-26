package se.tink.backend.aggregation.nxgen.agents;

import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.ProviderSessionCacheController;
import se.tink.backend.aggregation.nxgen.instrumentation.FetcherInstrumentationRegistry;

public abstract class NextGenerationAgent extends SubsequentGenerationAgent<Authenticator> {

    protected final ProviderSessionCacheController providerSessionCacheController;
    private Authenticator authenticator;

    protected NextGenerationAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        this.providerSessionCacheController =
                new ProviderSessionCacheController(providerSessionCacheContext);
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
