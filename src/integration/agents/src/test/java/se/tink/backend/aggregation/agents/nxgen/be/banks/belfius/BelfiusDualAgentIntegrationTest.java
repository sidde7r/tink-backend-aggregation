package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.DualAgentIntegrationTest;

public class BelfiusDualAgentIntegrationTest {

    private enum Arg {
        CARD_NUMBER,
        PASSWORD,
        IBAN
    }

    private final ArgumentManager<Arg> manager = new ArgumentManager<>(Arg.values());

    @Before
    public void setup() {
        manager.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void dualTest() throws Exception {

        DualAgentIntegrationTest.of(
                        new AgentIntegrationTest.Builder("be", "be-belfius-oauth2")
                                .addCredentialField("iban", manager.get(Arg.IBAN))
                                .expectLoggedIn(false)
                                .loadCredentialsBefore(false)
                                .saveCredentialsAfter(false)
                                .build(),

                        new AgentIntegrationTest.Builder("be", "be-belfius-cardreader")
                            .addCredentialField(Field.Key.USERNAME, manager.get(Arg.CARD_NUMBER))
                            .addCredentialField(Field.Key.PASSWORD, manager.get(Arg.PASSWORD))
                                .expectLoggedIn(false)
                                .loadCredentialsBefore(false)
                                .saveCredentialsAfter(true)
                                .build())
                .testAndCompare();
    }
}

