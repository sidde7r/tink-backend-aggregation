package se.tink.backend.aggregation.agents.nxgen.be.banks.ing;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentHelper;

public class IngAgentTest {

    private final ArgumentHelper helper = new ArgumentHelper("tink.username", "tink.cardId");

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
        new AgentIntegrationTest.Builder("be", "be-ing-cardreader")
                .addCredentialField(Field.Key.USERNAME, helper.get("tink.username"))
                .addCredentialField("cardId", helper.get("tink.cardId"))
                .loadCredentialsBefore(true)
                .saveCredentialsAfter(true)
                .build()
                .testRefresh();
    }
}
