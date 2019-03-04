package se.tink.backend.aggregation.agents.nxgen.be.banks.axa;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

public class AxaAgentTest {
    private enum Arg {
        LOAD_BEFORE,
        SAVE_AFTER,
        CARD_NUMBER,
    }

    private final ArgumentManager<Arg> manager = new ArgumentManager<>(Arg.values());

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
        new AgentIntegrationTest.Builder("be", "be-axa-cardreader")
                .addCredentialField(Field.Key.USERNAME, manager.get(Arg.CARD_NUMBER))
                .loadCredentialsBefore(Boolean.parseBoolean(manager.get(Arg.LOAD_BEFORE)))
                .saveCredentialsAfter(Boolean.parseBoolean(manager.get(Arg.SAVE_AFTER)))
                .build()
                .testRefresh();
    }
}
