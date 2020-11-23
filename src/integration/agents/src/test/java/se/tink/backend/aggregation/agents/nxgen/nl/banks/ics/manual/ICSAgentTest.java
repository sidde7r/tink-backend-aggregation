package se.tink.backend.aggregation.agents.nxgen.nl.banks.ics.manual;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest.Builder;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.LoadBeforeSaveAfterArgumentEnum;

public class ICSAgentTest {
    private final ArgumentManager<LoadBeforeSaveAfterArgumentEnum> manager =
            new ArgumentManager<>(LoadBeforeSaveAfterArgumentEnum.values());

    @Before
    public void before() {
        manager.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void refresh() throws Exception {
        final AgentIntegrationTest test =
                new Builder("nl", "nl-ics-oauth2")
                        .loadCredentialsBefore(
                                Boolean.parseBoolean(
                                        manager.get(LoadBeforeSaveAfterArgumentEnum.LOAD_BEFORE)))
                        .saveCredentialsAfter(
                                Boolean.parseBoolean(
                                        manager.get(LoadBeforeSaveAfterArgumentEnum.SAVE_AFTER)))
                        .expectLoggedIn(false)
                        .setFinancialInstitutionId("ics")
                        .setAppId("tink")
                        .build();

        test.testRefresh();
    }
}
