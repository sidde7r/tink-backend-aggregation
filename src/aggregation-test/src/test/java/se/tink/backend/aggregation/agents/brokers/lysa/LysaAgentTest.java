package se.tink.backend.aggregation.agents.brokers.lysa;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentHelper;
import se.tink.backend.aggregation.rpc.Field;

@Ignore
public class LysaAgentTest
{
    private final AgentIntegrationTest.Builder builder =
            new AgentIntegrationTest.Builder("se", "lysa")
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
