package se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.PsuIdArgumentEnum;

public class VolksbankAgentTest {
    private final ArgumentManager<PsuIdArgumentEnum> manager =
            new ArgumentManager<>(PsuIdArgumentEnum.values());

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        manager.before();

        builder =
                new AgentIntegrationTest.Builder("at", "at-volksbank-ob")
                        // TEST_PSU_ID => "100002"
                        .addCredentialField(Key.LOGIN_INPUT, manager.get(PsuIdArgumentEnum.PSU_ID))
                        // TEST_PSU_ID_TYPE => "disposer-nr"
                        .addCredentialField(
                                Key.LOGIN_DESCRIPTION, manager.get(PsuIdArgumentEnum.PSU_ID_TYPE))
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false);
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
