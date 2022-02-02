package se.tink.backend.aggregation.nxgen.controllers.payment.validation.impl;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentPisCapability;
import se.tink.backend.aggregation.agents.agentcapabilities.PisCapability;

@Ignore
@AgentPisCapability(
        capabilities = {
            PisCapability.SEPA_INSTANT_CREDIT_TRANSFER,
            PisCapability.PIS_SEPA_ICT_RECURRING_PAYMENTS
        })
public class InstantPayValidationTestAgent extends ValidationTestAgent {}
