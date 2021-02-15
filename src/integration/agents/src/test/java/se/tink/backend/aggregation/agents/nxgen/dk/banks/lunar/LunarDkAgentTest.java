package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

public class LunarDkAgentTest {

    private enum Arg implements ArgumentManager.ArgumentManagerEnum {
        USERNAME,
        PASSWORD,
        ACCESS_PIN;

        @Override
        public boolean isOptional() {
            return false;
        }
    }

    private final ArgumentManager<LunarDkAgentTest.Arg> manager =
            new ArgumentManager<>(LunarDkAgentTest.Arg.values());

    @Before
    public void before() {
        manager.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testLoginAndRefresh() throws Exception {
        new AgentIntegrationTest.Builder("dk", "dk-lunar-nemid")
                .addCredentialField(Field.Key.USERNAME, manager.get(Arg.USERNAME))
                .addCredentialField(Field.Key.PASSWORD, manager.get(Arg.PASSWORD))
                .addCredentialField(Field.Key.ACCESS_PIN, manager.get(Arg.ACCESS_PIN))
                .expectLoggedIn(false)
                .loadCredentialsBefore(true)
                .saveCredentialsAfter(true)
                .build()
                .testRefresh();
    }
}
