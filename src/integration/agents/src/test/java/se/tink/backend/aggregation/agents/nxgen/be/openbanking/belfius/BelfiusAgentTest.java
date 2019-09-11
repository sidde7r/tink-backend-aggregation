package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.CredentialKeys;

public class BelfiusAgentTest {

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
                new AgentIntegrationTest.Builder("be", "be-belfius-oauth2")
                        .addCredentialField(CredentialKeys.IBAN, manager.get(Arg.IBAN))
                        .expectLoggedIn(false)
                        .setFinancialInstitutionId("tink")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }

    private enum Arg {
        IBAN
    }
}
