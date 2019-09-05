package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

public class CommerzbankAgentTest {
    private final ArgumentManager<CommerzbankAgentTest.Arg> manager =
            new ArgumentManager<>(CommerzbankAgentTest.Arg.values());

    private AgentIntegrationTest.Builder builder;

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void before() {
        manager.before();

        builder =
                new AgentIntegrationTest.Builder("de", "de-commerzbank-password")
                        .loadCredentialsBefore(
                                Boolean.parseBoolean(
                                        manager.get(CommerzbankAgentTest.Arg.LOAD_BEFORE)))
                        .saveCredentialsAfter(
                                Boolean.parseBoolean(
                                        manager.get(CommerzbankAgentTest.Arg.SAVE_AFTER)));
    }

    @Test
    public void testLogin() throws Exception {
        builder.addCredentialField(
                        Field.Key.USERNAME, manager.get(CommerzbankAgentTest.Arg.USERNAME))
                .addCredentialField(
                        Field.Key.PASSWORD, manager.get(CommerzbankAgentTest.Arg.PASSWORD))
                .build()
                .testRefresh();
    }

    private enum Arg {
        LOAD_BEFORE,
        SAVE_AFTER,
        USERNAME,
        PASSWORD,
    }
}
