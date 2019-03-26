package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentHelper;

public class HVBAgentTest {
    private final ArgumentHelper helper;

    public HVBAgentTest() {
        helper = new ArgumentHelper("tink.username", "tink.password");
    }

    private final AgentIntegrationTest.Builder builder =
            new AgentIntegrationTest.Builder("de", "de-hypovereinsbank-password")
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
