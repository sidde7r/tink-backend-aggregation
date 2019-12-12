package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.ArgumentManagerEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernameArgumentEnum;

public class SparebankenSorAgentTest {
    private enum Arg implements ArgumentManagerEnum {
        MOBILENUMBER;

        @Override
        public boolean isOptional() {
            return false;
        }
    }

    private final ArgumentManager<Arg> manager = new ArgumentManager<>(Arg.values());
    private final ArgumentManager<UsernameArgumentEnum> usernameManager =
            new ArgumentManager<>(UsernameArgumentEnum.values());

    @Before
    public void setUp() throws Exception {
        manager.before();
        usernameManager.before();
    }

    @Test
    public void testLogin() throws Exception {
        new AgentIntegrationTest.Builder("no", "no-sparebankensor-bankid")
                .addCredentialField(
                        Field.Key.USERNAME, usernameManager.get(UsernameArgumentEnum.USERNAME))
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
