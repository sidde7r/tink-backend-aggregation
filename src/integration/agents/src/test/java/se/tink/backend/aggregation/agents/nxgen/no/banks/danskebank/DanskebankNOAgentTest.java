package se.tink.backend.aggregation.agents.nxgen.no.banks.danskebank;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

public class DanskebankNOAgentTest {

    private enum Arg implements ArgumentManager.ArgumentManagerEnum {
        USERNAME,
        PASSWORD,
        BANKID_PASSWORD;

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
        new AgentIntegrationTest.Builder("no", "no-danskebank-password")
                .addCredentialField(Field.Key.USERNAME, manager.get(Arg.USERNAME))
                .addCredentialField(Field.Key.PASSWORD, manager.get(Arg.PASSWORD))
                .addCredentialField(Field.Key.BANKID_PASSWORD, manager.get(Arg.BANKID_PASSWORD))
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(true)
                .build()
                .testRefresh();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }
}
