package se.tink.backend.aggregation.agents.agentcapabilities;

import org.junit.Ignore;

@Ignore
@AgentPisCapability(
        capabilities = {
            PisCapability.SEPA_CREDIT_TRANSFER,
            PisCapability.SEPA_INSTANT_CREDIT_TRANSFER
        })
public class TestAgentWithListedPisCapabilities extends BaseTestAgent {}
