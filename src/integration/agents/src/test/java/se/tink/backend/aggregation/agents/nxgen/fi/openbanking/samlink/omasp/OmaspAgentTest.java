package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.samlink.omasp;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

public class OmaspAgentTest {
    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("fi", "fi-omasp-ob")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .setFinancialInstitutionId("7ec2ca5411f645ecad992932e17e0593")
                        .setAppId("tink")
                        .saveCredentialsAfter(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }
}
