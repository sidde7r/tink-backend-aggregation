package se.tink.backend.aggregation.agents.nxgen.no.banks.dnbbankid;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.ArgumentManagerEnum;

public class DnbAgentTest {

    private enum Arg implements ArgumentManagerEnum {
        USERNAME,
        MOBILENUMBER;

        @Override
        public boolean isOptional() {
            return false;
        }
    }

    private final ArgumentManager<Arg> manager = new ArgumentManager<>(Arg.values());

    @Before
    public void setUp() throws Exception {
        manager.before();
    }

    @Test
    public void testLogin() throws Exception {
        new AgentIntegrationTest.Builder("no", "no-dnb-bankid")
                .addCredentialField(Field.Key.USERNAME, manager.get(Arg.USERNAME))
                .addCredentialField(Field.Key.MOBILENUMBER, manager.get(Arg.MOBILENUMBER))
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .build()
                .testRefresh();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }
}
