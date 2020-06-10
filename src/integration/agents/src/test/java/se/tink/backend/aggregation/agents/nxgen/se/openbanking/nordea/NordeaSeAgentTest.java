package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.LoadBeforeSaveAfterArgumentEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.SsnArgumentEnum;

public class NordeaSeAgentTest {

    private AgentIntegrationTest.Builder builder;
    private final ArgumentManager<LoadBeforeSaveAfterArgumentEnum> loadBeforeSaveAfterManager =
            new ArgumentManager<>(LoadBeforeSaveAfterArgumentEnum.values());
    private final ArgumentManager<SsnArgumentEnum> ssnManager =
            new ArgumentManager<>(SsnArgumentEnum.values());

    @Before
    public void setup() {
        ssnManager.before();
        loadBeforeSaveAfterManager.before();
        builder =
                new AgentIntegrationTest.Builder("SE", "se-nordea-ob")
                        .addCredentialField(Field.Key.USERNAME, ssnManager.get(SsnArgumentEnum.SSN))
                        .expectLoggedIn(false)
                        .setFinancialInstitutionId("nordea")
                        .setAppId("tink")
                        .loadCredentialsBefore(
                                Boolean.parseBoolean(
                                        loadBeforeSaveAfterManager.get(
                                                LoadBeforeSaveAfterArgumentEnum.LOAD_BEFORE)))
                        .saveCredentialsAfter(
                                Boolean.parseBoolean(
                                        loadBeforeSaveAfterManager.get(
                                                LoadBeforeSaveAfterArgumentEnum.SAVE_AFTER)));
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
