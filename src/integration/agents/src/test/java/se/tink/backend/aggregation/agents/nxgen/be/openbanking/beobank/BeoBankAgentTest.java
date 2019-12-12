package se.tink.backend.aggregation.agents.nxgen.be.openbanking.beobank;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.LoadBeforeSaveAfterArgumentEnum;

public class BeoBankAgentTest {

    private final ArgumentManager<LoadBeforeSaveAfterArgumentEnum> manager =
            new ArgumentManager<>(LoadBeforeSaveAfterArgumentEnum.values());
    private AgentIntegrationTest.Builder builder;

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void setup() {
        manager.before();
        builder =
                new AgentIntegrationTest.Builder("be", "be-beobank-ob")
                        .setFinancialInstitutionId("beobank")
                        .setAppId("tink")
                        .loadCredentialsBefore(
                                Boolean.parseBoolean(
                                        manager.get(LoadBeforeSaveAfterArgumentEnum.LOAD_BEFORE)))
                        .saveCredentialsAfter(
                                Boolean.parseBoolean(
                                        manager.get(LoadBeforeSaveAfterArgumentEnum.SAVE_AFTER)))
                        .expectLoggedIn(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
