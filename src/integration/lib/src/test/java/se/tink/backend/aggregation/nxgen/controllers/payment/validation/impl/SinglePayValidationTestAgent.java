package se.tink.backend.aggregation.nxgen.controllers.payment.validation.impl;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentPisCapability;
import se.tink.backend.aggregation.agents.agentcapabilities.PisCapability;

@Ignore
@AgentPisCapability(
        capabilities = {
            PisCapability.SEPA_CREDIT_TRANSFER,
            PisCapability.SEPA_INSTANT_CREDIT_TRANSFER
        })
public class SinglePayValidationTestAgent extends ValidationTestAgent {}
