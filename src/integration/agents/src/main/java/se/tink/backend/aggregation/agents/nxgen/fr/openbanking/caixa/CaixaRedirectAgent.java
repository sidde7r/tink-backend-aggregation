package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.caixa;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.common.signature.QSealSignatureProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsModule;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsProgressiveBaseAgent;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.http.client.LoggingStrategy;

@AgentDependencyModules(modules = SibsModule.class)
@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS})
public final class CaixaRedirectAgent extends SibsProgressiveBaseAgent {

    @Inject
    public CaixaRedirectAgent(
            AgentComponentProvider agentComponentProvider,
            AgentsServiceConfiguration configuration,
            QSealSignatureProvider qSealSignatureProvider) {
        super(agentComponentProvider, configuration, qSealSignatureProvider);

        setJsonHttpTrafficLogsEnabled(true);
        client.setLoggingStrategy(LoggingStrategy.EXPERIMENTAL);
    }
}
