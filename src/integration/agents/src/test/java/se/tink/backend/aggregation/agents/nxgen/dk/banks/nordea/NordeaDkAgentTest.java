package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernamePasswordArgumentEnum;

public class NordeaDkAgentTest {
    private final ArgumentManager<UsernamePasswordArgumentEnum> manager =
            new ArgumentManager<>(UsernamePasswordArgumentEnum.values());

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
        new AgentIntegrationTest.Builder("dk", "dk-nordea-nemid")
                .addCredentialField(
                        Field.Key.USERNAME, manager.get(UsernamePasswordArgumentEnum.USERNAME))
                .addCredentialField(
                        Field.Key.PASSWORD, manager.get(UsernamePasswordArgumentEnum.PASSWORD))
                .expectLoggedIn(false)
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .build()
                .testRefresh();
    }
}
