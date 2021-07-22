package se.tink.backend.aggregation.agents.nxgen.lv.openbanking.citadele;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import java.util.Collection;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.CitadeleBaseAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CHECKING_ACCOUNTS})
public class CitadeleLVAgent extends CitadeleBaseAgent {

    @Inject
    protected CitadeleLVAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
    }

    @Override
    protected Collection<String> getSupportedLocales() {
        return ImmutableSet.of("lv_LV", "ru_LV", "en_LV");
    }
}
