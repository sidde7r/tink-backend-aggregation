package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentHelper;
import se.tink.backend.aggregation.rpc.Field;

public class BawagPskAgentTest {
    private final ArgumentHelper helper = new ArgumentHelper("tink.username", "tink.password");

    private final AgentIntegrationTest.Builder builder = new AgentIntegrationTest.Builder("at",
            "at-bawagpsk-password")
            .loadCredentialsBefore(false)
            .saveCredentialsAfter(false);

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
        builder.addCredentialField(Field.Key.USERNAME, helper.get("tink.username"))
                .addCredentialField(Field.Key.PASSWORD, helper.get("tink.password"))
                .build()
                .testRefresh();
    }
}