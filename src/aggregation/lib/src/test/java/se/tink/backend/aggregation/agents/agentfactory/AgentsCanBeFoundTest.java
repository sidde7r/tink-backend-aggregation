package se.tink.backend.aggregation.agents.agentfactory;

import org.junit.Test;
import se.tink.backend.aggregation.agents.agentfactory.impl.AgentClassFactory;

public final class AgentsCanBeFoundTest {

    /**
     * Test to ensure an agent class contained in its own agent-specific Bazel package can be found
     * from the aggregation package.
     */
    @Test
    public void testIsolatedAgentClassIsFound() throws ClassNotFoundException {
        AgentClassFactory.getAgentClass("nxgen.be.banks.axa.AxaAgent");
        AgentClassFactory.getAgentClass("nxgen.uk.openbanking.ukob.aib.AibV31Agent");
        AgentClassFactory.getAgentClass("nxgen.uk.openbanking.danskebank.DanskeBankV31Agent");
        AgentClassFactory.getAgentClass(
                "nxgen.uk.openbanking.ukob.hsbcgroup.firstdirect.FirstDirectV31Agent");
    }
}
