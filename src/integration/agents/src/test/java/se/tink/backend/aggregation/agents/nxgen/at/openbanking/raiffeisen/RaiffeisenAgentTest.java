package se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

public class RaiffeisenAgentTest {

    private AgentIntegrationTest.Builder builder;
    private final ArgumentManager<Arg> manager = new ArgumentManager<>(Arg.values());

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void setup() {
        manager.before();

        builder =
                new AgentIntegrationTest.Builder("at", "at-raiffeisen-oauth2")
                        .addCredentialField(
                                RaiffeisenConstants.CredentialKeys.IBAN, manager.get(Arg.IBAN))
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(true)
                        .saveCredentialsAfter(true);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }

    private enum Arg {
        IBAN
    }
}
