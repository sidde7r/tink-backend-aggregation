package se.tink.backend.aggregation.agents.nxgen.demo.banks.bankid;

import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class BankIdDemoAgentTest {

    private static final String USERNAME = "180012120000";

    @Test
    public void refresh() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("se", "se-test-bankid-qr-successful")
                        .addCredentialField(Field.Key.USERNAME, USERNAME)
                        .setFinancialInstitutionId("dummy")
                        .setAppId("dummy")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false);

        builder.build().testRefresh();
    }
}
