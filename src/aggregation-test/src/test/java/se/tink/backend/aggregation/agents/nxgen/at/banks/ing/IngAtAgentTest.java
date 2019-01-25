package se.tink.backend.aggregation.agents.nxgen.at.banks.ing;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentHelper;

public class IngAtAgentTest {
    private final ArgumentHelper helper = new ArgumentHelper("tink.username", "tink.password");

    @Before
    public void before() {
        helper.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentHelper.afterClass();
    }

    @Test
    public void testLoginAndRefresh() throws Exception {
        new AgentIntegrationTest.Builder("at", "at-ingdiba-password")
                .addCredentialField(Field.Key.USERNAME, helper.get("tink.username"))
                .addCredentialField(Field.Key.PASSWORD, helper.get("tink.password"))
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .doLogout(true)
                .transactionsToPrint(0) // 0 ~ no limit
                .build()
                .testRefresh();
    }
}
