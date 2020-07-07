package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.samlink.saastopankki;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

public class SpAgentTest {
    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("fi", "fi-saastopankki-ob")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .setFinancialInstitutionId("c1746bbd12204b5d959424d2c598cddc")
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
