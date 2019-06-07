package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public abstract class SebAgentBaseTest {

    // To get sandbox identification number please visit https://developer.sebgroup.com/node/2187

    private AgentIntegrationTest.Builder builder;

    private String providerName;

    public SebAgentBaseTest(String providerName) {
        this.providerName = providerName;
    }

    @Before
    public void setup() {
        builder = new AgentIntegrationTest.Builder("se", providerName).expectLoggedIn(true);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
