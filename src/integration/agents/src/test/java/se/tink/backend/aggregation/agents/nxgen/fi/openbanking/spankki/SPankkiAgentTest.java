package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.spankki;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.SsnArgumentEnum;

public class SPankkiAgentTest {

    private AgentIntegrationTest.Builder builder;
    private final ArgumentManager<SsnArgumentEnum> manager =
            new ArgumentManager<>(SsnArgumentEnum.values());

    @Before
    public void setup() {
        manager.before();
        builder =
                new AgentIntegrationTest.Builder("fi", "fi-spankki-oauth2")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .setFinancialInstitutionId("s-pankki")
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
