package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.BerlinGroupTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.configuration.SamlinkAgentsConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.configuration.SamlinkConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.transactionalaccount.SamlinkTransactionFetcher;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationFlow;

public class SamlinkAgent extends BerlinGroupAgent<SamlinkApiClient, SamlinkConfiguration> {

    private final QsealcSigner qsealcSigner;
    private final SamlinkAgentsConfiguration agentConfiguration;

    public SamlinkAgent(
            AgentComponentProvider componentProvider,
            QsealcSigner qsealcSigner,
            SamlinkAgentsConfiguration agentConfiguration) {
        super(componentProvider);

        this.agentConfiguration = agentConfiguration;
        this.qsealcSigner = qsealcSigner;
        this.apiClient = createApiClient();
        this.transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    @Override
    protected SamlinkApiClient createApiClient() {
        return new SamlinkApiClient(
                client,
                persistentStorage,
                qsealcSigner,
                getConfiguration(),
                getConfiguration().getProviderSpecificConfiguration(),
                request,
                agentConfiguration);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return OAuth2AuthenticationFlow.create(
                request,
                systemUpdater,
                persistentStorage,
                supplementalInformationHelper,
                new SamlinkAuthenticator(apiClient),
                credentials,
                strongAuthenticationState);
    }

    @Override
    protected Class<SamlinkConfiguration> getConfigurationClassDescription() {
        return SamlinkConfiguration.class;
    }

    @Override
    protected BerlinGroupTransactionFetcher getTransactionFetcher() {
        return new SamlinkTransactionFetcher(apiClient, agentConfiguration);
    }
}
