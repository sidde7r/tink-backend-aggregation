package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.authenticator.TriodosAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.configuration.TriodosConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupAgent;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.module.QSealcSignerModuleRSASHA256;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationFlow;

@AgentDependencyModules(modules = QSealcSignerModuleRSASHA256.class)
@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS})
public final class TriodosAgent extends BerlinGroupAgent<TriodosApiClient, TriodosConfiguration> {

    private final QsealcSigner qsealcSigner;

    @Inject
    public TriodosAgent(AgentComponentProvider componentProvider, QsealcSigner qsealcSigner) {
        super(componentProvider);

        this.qsealcSigner = qsealcSigner;
        this.apiClient = createApiClient();
        this.transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    @Override
    protected TriodosApiClient createApiClient() {
        return new TriodosApiClient(
                client,
                persistentStorage,
                getConfiguration().getProviderSpecificConfiguration(),
                request,
                getConfiguration().getRedirectUrl(),
                qsealcSigner,
                getConfiguration().getQsealc());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return OAuth2AuthenticationFlow.create(
                request,
                systemUpdater,
                persistentStorage,
                supplementalInformationHelper,
                new TriodosAuthenticator(apiClient, persistentStorage),
                credentials,
                strongAuthenticationState);
    }

    @Override
    protected Class<TriodosConfiguration> getConfigurationClassDescription() {
        return TriodosConfiguration.class;
    }
}
