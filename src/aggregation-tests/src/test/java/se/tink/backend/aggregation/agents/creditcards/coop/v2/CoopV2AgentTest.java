package se.tink.backend.aggregation.agents.creditcards.coop.v2;

import org.junit.Test;
import se.tink.backend.aggregation.agents.AbstractAgentTest;

public class CoopV2AgentTest extends AbstractAgentTest<CoopV2Agent> {
    public CoopV2AgentTest() {
        super(CoopV2Agent.class);
    }

    @Test
    public void testAgent() throws Exception {
        testAgent("name@example.com", "mypassword");
    }
}
