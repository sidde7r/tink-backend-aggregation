package se.tink.backend.aggregation.agents.agentcapabilities;

import org.junit.Ignore;
import se.tink.backend.aggregation.client.provider_configuration.rpc.PisCapability;

@Ignore
@AgentPisCapability(capabilities = {PisCapability.PIS_SEPA, PisCapability.PIS_SEPA_ICT})
public class TestAgentWithListedPisCapabilities extends BaseTestAgent {}
