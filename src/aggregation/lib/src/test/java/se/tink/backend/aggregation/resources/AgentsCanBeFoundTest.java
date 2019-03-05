package se.tink.backend.aggregation.resources;

import org.junit.Test;
import se.tink.backend.aggregation.agents.AgentClassFactory;

public final class AgentsCanBeFoundTest {

    /**
     * Test to ensure an agent class in the integration/agents Bazel package can be found from the
     * aggregation package.
     */
    @Test
    public void testAgentsPackageClassIsFound() throws ClassNotFoundException {
        AgentClassFactory.getAgentClass("nxgen.at.banks.easybank.bawagpsk.BawagPskAgent");
    }

    /**
     * Test to ensure an agent class contained in its own agent-specific Bazel package can be found
     * from the aggregation package.
     */
    @Test
    public void testIsolatedAgentClassIsFound() throws ClassNotFoundException {
        AgentClassFactory.getAgentClass("nxgen.be.banks.axa.AxaAgent");
    }
}
