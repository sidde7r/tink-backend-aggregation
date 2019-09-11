package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.DualAgentIntegrationTest;

public class SparebankenSorDualAgentTest {
    private enum Arg {
        USERNAME,
        MOBILENUMBER
    }

    private final ArgumentManager<Arg> manager = new ArgumentManager<>(Arg.values());

    @Before
    public void setUp() throws Exception {
        manager.before();
    }

    @Test
    public void testLogin() throws Exception {
        DualAgentIntegrationTest.of(
                        new AgentIntegrationTest.Builder("no", "no-sparebankensor-bankid")
                                .addCredentialField(Field.Key.USERNAME, manager.get(Arg.USERNAME))
                                .addCredentialField(
                                        Field.Key.MOBILENUMBER, manager.get(Arg.MOBILENUMBER))
                                .loadCredentialsBefore(false)
                                .saveCredentialsAfter(true)
                                .expectLoggedIn(false)
                                .setFinancialInstitutionId("dummy")
                                .build(),
                        new AgentIntegrationTest.Builder("no", "no-sparebankensor-ob")
                                .expectLoggedIn(false)
                                .loadCredentialsBefore(false)
                                .saveCredentialsAfter(false)
                                .build())
                .testAndCompare();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }
}
