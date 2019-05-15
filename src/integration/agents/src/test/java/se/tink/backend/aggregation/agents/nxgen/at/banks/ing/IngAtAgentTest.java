package se.tink.backend.aggregation.agents.nxgen.at.banks.ing;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

public class IngAtAgentTest {
    private enum Arg {
        USERNAME,
        PASSWORD,
    }

    private final ArgumentManager<Arg> helper = new ArgumentManager<>(Arg.values());

    @Before
    public void before() {
        helper.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testLoginAndRefresh() throws Exception {
        new AgentIntegrationTest.Builder("at", "at-ingdiba-password")
                .addCredentialField(Field.Key.USERNAME, helper.get(Arg.USERNAME))
                .addCredentialField(Field.Key.PASSWORD, helper.get(Arg.PASSWORD))
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .doLogout(true)
                .transactionsToPrint(0) // 0 ~ no limit
                .build()
                .testRefresh();
    }
}
