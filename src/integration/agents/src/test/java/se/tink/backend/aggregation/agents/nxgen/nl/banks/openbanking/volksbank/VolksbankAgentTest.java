package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

public final class VolksbankAgentTest {
    private enum Arg {
        LOAD_BEFORE,
        SAVE_AFTER,
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

    private AgentIntegrationTest createAgentTest(final String providerName) {
        return new AgentIntegrationTest.Builder("nl", providerName)
                .loadCredentialsBefore(Boolean.parseBoolean(manager.get(Arg.LOAD_BEFORE)))
                .saveCredentialsAfter(Boolean.parseBoolean(manager.get(Arg.SAVE_AFTER)))
                .expectLoggedIn(false)
                .build();
    }

    @Test
    public void testSnsBank() throws Exception {
        createAgentTest("nl-snsbank-ob").testRefresh();
    }

    @Test
    public void testAsnBank() throws Exception {
        createAgentTest("nl-asnbank-ob").testRefresh();
    }

    @Test
    public void testRegioBank() throws Exception {
        createAgentTest("nl-regiobank-ob").testRefresh();
    }

    @Test
    public void testSnsBankSandbox() throws Exception {
        createAgentTest("nl-snsbank-sandbox-ob").testRefresh();
    }
}
