package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

public class KbcAgentTest {

    private AgentIntegrationTest.Builder builder;

    private enum Arg {
        IBAN,
        LOAD_BEFORE,
        SAVE_AFTER,
    }

    private final ArgumentManager<Arg> manager = new ArgumentManager<>(Arg.values());

    @Before
    public void setup() {
        manager.before();
        builder =
                new AgentIntegrationTest.Builder("be", "be-kbc-ob")
                        .addCredentialField(KbcConstants.CredentialKeys.IBAN, manager.get(Arg.IBAN))
                        .setFinancialInstitutionId("kbc")
                        .setAppId("tink")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(Boolean.parseBoolean(manager.get(Arg.LOAD_BEFORE)))
                        .saveCredentialsAfter(Boolean.parseBoolean(manager.get(Arg.SAVE_AFTER)));
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
