package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public abstract class SebAgentBaseTest {

    private AgentIntegrationTest.Builder builder;

    private String providerName;

    private String market;

    public SebAgentBaseTest(String providerName, String market) {
        this.providerName = providerName;
        this.market = market;
    }

    @Before
    public void setup() {
        builder = new AgentIntegrationTest.Builder(market, providerName).expectLoggedIn(true);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
