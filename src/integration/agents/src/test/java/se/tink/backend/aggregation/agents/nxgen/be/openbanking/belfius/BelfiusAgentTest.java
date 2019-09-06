package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.CredentialKeys;

public class BelfiusAgentTest {

    private final ArgumentManager<Arg> manager = new ArgumentManager<>(Arg.values());
    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        manager.before();

        builder =
                new AgentIntegrationTest.Builder("be", "be-belfius-ob")
                        .addCredentialField(CredentialKeys.USERNAME, manager.get(Arg.USERNAME))
                        .addCredentialField(CredentialKeys.PASSWORD, manager.get(Arg.PASSWORD))
                        .addCredentialField(CredentialKeys.IBAN, manager.get(Arg.IBAN))
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }

    private enum Arg {
        USERNAME,
        PASSWORD,
        IBAN
    }
}
