package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.agent;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

public class NordeaNoAgentTest {

    private enum Arg implements ArgumentManager.ArgumentManagerEnum {
        USERNAME,
        PASSWORD;

        private final boolean optional;

        Arg() {
            this.optional = false;
        }

        @Override
        public boolean isOptional() {
            return optional;
        }
    }

    private final ArgumentManager<Arg> manager = new ArgumentManager<>(Arg.values());

    @Before
    public void setUp() throws Exception {
        manager.before();
    }

    @Test
    public void testRefreshBankId() throws Exception {
        new AgentIntegrationTest.Builder("no", "no-nordea-bankid")
                .addCredentialField(Field.Key.USERNAME, manager.get(Arg.USERNAME))
                .addCredentialField(Field.Key.BANKID_PASSWORD, manager.get(Arg.PASSWORD))
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
