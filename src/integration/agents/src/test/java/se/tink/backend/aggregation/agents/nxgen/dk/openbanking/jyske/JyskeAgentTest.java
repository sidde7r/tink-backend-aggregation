package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.jyske;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataConstants;

public class JyskeAgentTest {

    // IBAN for configuration DK5099991234567890
    // --jvmopt=-Dtink.IBAN=DK5099991234567890
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
                new AgentIntegrationTest.Builder("dk", "dk-jyske-oauth2")
                        .addCredentialField(
                                BankdataConstants.CredentialKeys.IBAN, manager.get(Arg.IBAN))
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }

    private enum Arg {
        IBAN
    }
}
