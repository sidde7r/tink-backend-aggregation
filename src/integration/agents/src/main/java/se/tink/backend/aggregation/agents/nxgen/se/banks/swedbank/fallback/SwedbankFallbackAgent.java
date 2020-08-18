package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fallback;

import com.google.inject.Inject;
import java.time.ZoneId;
import java.util.Locale;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fallback.configuration.SwedbankFallbackConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fallback.configuration.SwedbankPsd2Configuration;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fallback.filter.SwedbankFallbackHttpFilter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankAbstractAgent;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.utilities.SwedbankDateUtils;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.eidassigner.module.QSealcSignerModuleRSASHA256;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentDependencyModules(modules = QSealcSignerModuleRSASHA256.class)
public class SwedbankFallbackAgent extends SwedbankAbstractAgent {

    @Inject
    protected SwedbankFallbackAgent(
            AgentComponentProvider componentProvider,
            AgentsServiceConfiguration agentsServiceConfiguration) {
        super(
                componentProvider,
                new SwedbankFallbackConfiguration(
                        componentProvider.getCredentialsRequest().getProvider().getPayload()),
                new SwedbankFallbackApiClientProvider(agentsServiceConfiguration),
                new SwedbankDateUtils(ZoneId.of("Europe/Stockholm"), new Locale("sv", "SE")));

        final AgentConfiguration agentConfiguration =
                getAgentConfigurationController()
                        .getAgentConfiguration(SwedbankPsd2Configuration.class);

        final SwedbankPsd2Configuration psd2Configuration =
                (SwedbankPsd2Configuration) agentConfiguration.getProviderSpecificConfiguration();

        final String qSealc = agentConfiguration.getQsealc();

        client.setEidasProxy(agentsServiceConfiguration.getEidasProxy());
        client.addFilter(
                new SwedbankFallbackHttpFilter(
                        psd2Configuration, agentsServiceConfiguration, getEidasIdentity(), qSealc));
    }
}
