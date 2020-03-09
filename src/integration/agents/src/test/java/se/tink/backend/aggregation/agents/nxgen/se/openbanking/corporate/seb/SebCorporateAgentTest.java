package se.tink.backend.aggregation.agents.nxgen.se.openbanking.corporate.seb;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.CorporateIdArgumentEnum;

public class SebCorporateAgentTest {
    private AgentIntegrationTest.Builder builder;
    private final ArgumentManager<CorporateIdArgumentEnum> manager =
            new ArgumentManager<>(CorporateIdArgumentEnum.values());

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void setup() {
        manager.before();
        builder =
                new AgentIntegrationTest.Builder("se", "se-sebcorporate-ob")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .setFinancialInstitutionId("sebcorporate")
                        .setAppId("tink")
                        .addCredentialField(
                                Key.CORPORATE_ID, manager.get(CorporateIdArgumentEnum.CPI))
                        .expectLoggedIn(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
