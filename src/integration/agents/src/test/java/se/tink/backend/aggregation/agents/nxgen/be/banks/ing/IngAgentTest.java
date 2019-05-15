package se.tink.backend.aggregation.agents.nxgen.be.banks.ing;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

public class IngAgentTest {

    private enum Arg {
        USERNAME,
        CARD_ID,
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
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("be", "be-ing-cardreader")
                .addCredentialField(Field.Key.USERNAME, helper.get(Arg.USERNAME))
                .addCredentialField("cardId", helper.get(Arg.CARD_ID))
                .loadCredentialsBefore(true)
                .saveCredentialsAfter(true)
                .build()
                .testRefresh();
    }
}
