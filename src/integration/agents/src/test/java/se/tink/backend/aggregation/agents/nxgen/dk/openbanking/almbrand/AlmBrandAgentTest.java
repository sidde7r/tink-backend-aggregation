package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.almbrand;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class AlmBrandAgentTest {

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("dk", "dk-almbrand-ob")
                        .setFinancialInstitutionId("almbrand")
                        .setAppId("tink")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
