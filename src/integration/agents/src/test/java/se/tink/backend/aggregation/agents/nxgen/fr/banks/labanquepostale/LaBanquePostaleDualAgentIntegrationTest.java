package se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.DualAgentIntegrationTest;

@Ignore
public class LaBanquePostaleDualAgentIntegrationTest {

    private enum Arg {
        USERNAME,
        PASSWORD,
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
                        new AgentIntegrationTest.Builder("fr", "fr-labanquepostale-oauth2")
                                .expectLoggedIn(false)
                                .loadCredentialsBefore(false)
                                .saveCredentialsAfter(false)
                                .build(),
                        new AgentIntegrationTest.Builder(
                                        LaBanquePostaleConstants.MARKET,
                                        LaBanquePostaleConstants.PROVIDER_NAME)
                                .addCredentialField(Field.Key.USERNAME, manager.get(Arg.USERNAME))
                                .addCredentialField(Field.Key.PASSWORD, manager.get(Arg.PASSWORD))
                                .loadCredentialsBefore(false)
                                .saveCredentialsAfter(false)
                                .build())
                .testAndCompare();
    }
}
