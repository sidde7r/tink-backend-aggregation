package se.tink.backend.aggregation.agents.nxgen.se.openbanking.business.seb;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.BusinessIdArgumentEnum;

public class SebSEBusinessAgentTest {
    private AgentIntegrationTest.Builder builder;
    private final ArgumentManager<BusinessIdArgumentEnum> manager =
            new ArgumentManager<>(BusinessIdArgumentEnum.values());

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void setup() {
        manager.before();
        builder =
                new AgentIntegrationTest.Builder("se", "se-seb-business-ob")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .setFinancialInstitutionId("sebcorporate")
                        .setAppId("tink")
                        .addCredentialField(
                                Key.CORPORATE_ID, manager.get(BusinessIdArgumentEnum.CPI))
                        .expectLoggedIn(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
