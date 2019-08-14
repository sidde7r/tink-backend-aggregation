package se.tink.backend.aggregation.agents.nxgen.se.banks.seb;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.DualAgentIntegrationTest;

public class SebDualAgentTest {

    private enum Arg {
        SSN,
    }

    private final ArgumentManager<Arg> manager = new ArgumentManager<>(Arg.values());

    private AgentIntegrationTest.Builder legacyTestBuilder;
    private AgentIntegrationTest.Builder nxgenTestBuilder;

    @Before
    public void before() {
        manager.before();

        legacyTestBuilder =
                new AgentIntegrationTest.Builder("se", "seb-bankid")
                        .addCredentialField(Field.Key.USERNAME, manager.get(Arg.SSN))
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);

        nxgenTestBuilder =
                new AgentIntegrationTest.Builder("se", "se-seb-bankid")
                        .addCredentialField(Field.Key.USERNAME, manager.get(Arg.SSN))
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testLoginAndRefresh() throws Exception {
        final DualAgentIntegrationTest test =
                DualAgentIntegrationTest.of(legacyTestBuilder.build(), nxgenTestBuilder.build());
        test.testAndCompare();
    }
}
