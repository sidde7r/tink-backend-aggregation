package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

public class BunqAgentTest {
    private final ArgumentManager<Arg> manager = new ArgumentManager<>(Arg.values());
    private AgentIntegrationTest.Builder builder;

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void before() {
        manager.before();

        builder =
                new AgentIntegrationTest.Builder("nl", "nl-bunq-oauth2")
                        .loadCredentialsBefore(Boolean.parseBoolean(manager.get(Arg.LOAD_BEFORE)))
                        .saveCredentialsAfter(Boolean.parseBoolean(manager.get(Arg.SAVE_AFTER)));
    }

    @Test
    public void refresh() throws Exception {
        builder.expectLoggedIn(false).build().testRefresh();
    }

    private enum Arg {
        LOAD_BEFORE,
        SAVE_AFTER,
    }
}
