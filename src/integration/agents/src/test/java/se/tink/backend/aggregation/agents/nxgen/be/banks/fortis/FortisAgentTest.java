package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

public class FortisAgentTest {

    private final ArgumentManager<FortisAgentTest.Arg> manager =
            new ArgumentManager<>(FortisAgentTest.Arg.values());

    @Before
    public void setUp() throws Exception {
        manager.before();
    }

    @Test
    public void testRegisterAndRefresh() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("be", "be-fortis-cardreader")
                        .addCredentialField(Field.Key.USERNAME, manager.get(Arg.USERNAME))
                        .addCredentialField("clientnumber", manager.get(Arg.CLIENTNUMBER))
                        .loadCredentialsBefore(true)
                        .expectLoggedIn(false)
                        .saveCredentialsAfter(true);
        builder.build().testRefresh();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    private enum Arg {
        USERNAME,
        CLIENTNUMBER
    }
}
