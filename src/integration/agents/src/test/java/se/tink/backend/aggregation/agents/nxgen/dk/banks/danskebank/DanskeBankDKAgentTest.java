package se.tink.backend.aggregation.agents.nxgen.dk.banks.danskebank;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

public class DanskeBankDKAgentTest {
    private enum Arg {
        USERNAME,
        PASSWORD
    }

    private final ArgumentManager<Arg> manager = new ArgumentManager<>(Arg.values());

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setUp() throws Exception {
        manager.before();
    }

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("dk", "dk-danskebank-servicecode")
                .setFinancialInstitutionId("dsjdnjn")
                .addCredentialField(Field.Key.USERNAME, manager.get(Arg.USERNAME))
                .addCredentialField(Field.Key.PASSWORD, manager.get(Arg.PASSWORD))
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
