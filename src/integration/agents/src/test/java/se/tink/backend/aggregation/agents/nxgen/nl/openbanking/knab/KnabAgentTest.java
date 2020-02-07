package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.KnabConstants.CredentialKeys;

public class KnabAgentTest {

    private final ArgumentManager<Arg> manager = new ArgumentManager<>(Arg.values());
    private AgentIntegrationTest.Builder builder;

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void setup() {
        manager.before();
        builder =
                new AgentIntegrationTest.Builder("nl", "nl-knab-ob")
                        .addCredentialField(CredentialKeys.IBANS, manager.get(Arg.IBANS))
                        .loadCredentialsBefore(Boolean.parseBoolean(manager.get(Arg.LOAD_BEFORE)))
                        .saveCredentialsAfter(Boolean.parseBoolean(manager.get(Arg.SAVE_AFTER)))
                        .setFinancialInstitutionId("knab")
                        .setAppId("tink")
                        .expectLoggedIn(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }

    private enum Arg {
        IBANS,
        LOAD_BEFORE,
        SAVE_AFTER,
    }
}
