package se.tink.backend.aggregation.agents.banks.nordea;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentHelper;

public class NordeaV20AgentTest {
    private final AgentIntegrationTest.Builder builder =
            new AgentIntegrationTest.Builder("se", "nordea-bankid")
                    .loadCredentialsBefore(false)
                    .saveCredentialsAfter(true);
    private final ArgumentHelper helper = new ArgumentHelper("tink.username");

    @Before
    public void before() {
        helper.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentHelper.afterClass();
    }

    @Test
    public void testRefresh() throws Exception {
        builder.addCredentialField(Field.Key.USERNAME, helper.get("tink.username"))
                .build()
                .testRefresh();
    }
}
