package se.tink.backend.aggregation.agents.nxgen.de.openbanking.deutschebank;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.CredentialKeys;

public class DeutscheBankDEAgentTest {

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
                new AgentIntegrationTest.Builder("de", "de-deutschebank-ob")
                        .addCredentialField(CredentialKeys.IBAN, manager.get(Arg.IBAN))
                        .addCredentialField(CredentialKeys.PSU_ID, manager.get(Arg.PSU_ID))
                        .setFinancialInstitutionId("deutschebank")
                        .setAppId("tink")
                        .loadCredentialsBefore(false)
                        .expectLoggedIn(false)
                        .saveCredentialsAfter(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }

    private enum Arg {
        IBAN,
        PSU_ID,
    }
}
