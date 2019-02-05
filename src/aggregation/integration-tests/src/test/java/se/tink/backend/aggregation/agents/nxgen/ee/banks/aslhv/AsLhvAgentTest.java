package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentHelper;

public class AsLhvAgentTest {
    private final ArgumentHelper helper = new ArgumentHelper("tink.username", "tink.password");

    private final AgentIntegrationTest.Builder builder = new AgentIntegrationTest.Builder("ee",
            "ee-aslhv-password")
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
                .doLogout(true)
                .build()
                .testRefresh();
    }
}
