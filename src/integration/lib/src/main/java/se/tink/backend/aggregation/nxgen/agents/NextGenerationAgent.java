package se.tink.backend.aggregation.nxgen.agents;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.strategy.SubsequentGenerationAgentStrategy;
import se.tink.backend.aggregation.nxgen.agents.strategy.SubsequentGenerationAgentStrategyFactory;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.ProviderSessionCacheController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.libraries.credentials.service.CredentialsRequest;

public abstract class NextGenerationAgent extends SubsequentGenerationAgent<Authenticator> {

    protected final SupplementalInformationHelper supplementalInformationHelper;
    protected final SupplementalInformationController supplementalInformationController;
    protected final ProviderSessionCacheController providerSessionCacheController;
    private Authenticator authenticator;

    protected NextGenerationAgent(SubsequentGenerationAgentStrategy agentStrategy) {
        super(agentStrategy);
        SupplementalInformationProvider supplementalInformationProvider =
                new SupplementalInformationProvider(request, supplementalRequester, credentials);
        this.supplementalInformationController =
                supplementalInformationProvider.getSupplementalInformationController();
        this.supplementalInformationHelper =
                supplementalInformationProvider.getSupplementalInformationHelper();
        this.providerSessionCacheController =
                new ProviderSessionCacheController(providerSessionCacheContext);
    }

    protected NextGenerationAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(SubsequentGenerationAgentStrategyFactory.nxgen(request, context, signatureKeyPair));
        SupplementalInformationProvider supplementalInformationProvider =
                new SupplementalInformationProvider(request, supplementalRequester, credentials);
        this.supplementalInformationController =
                supplementalInformationProvider.getSupplementalInformationController();
        this.supplementalInformationHelper =
                supplementalInformationProvider.getSupplementalInformationHelper();
        this.providerSessionCacheController =
                new ProviderSessionCacheController(providerSessionCacheContext);
    }

    protected NextGenerationAgent(
            CredentialsRequest request,
            AgentContext context,
            SignatureKeyPair signatureKeyPair,
            boolean useLegacyTinkHttpClient) {
        super(SubsequentGenerationAgentStrategyFactory.legacy(request, context, signatureKeyPair));
        SupplementalInformationProvider supplementalInformationProvider =
                new SupplementalInformationProvider(request, supplementalRequester, credentials);
        this.supplementalInformationController =
                supplementalInformationProvider.getSupplementalInformationController();
        this.supplementalInformationHelper =
                supplementalInformationProvider.getSupplementalInformationHelper();
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
}
