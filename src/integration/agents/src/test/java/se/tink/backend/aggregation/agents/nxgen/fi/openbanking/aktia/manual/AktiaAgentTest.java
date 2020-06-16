package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.manual;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class AktiaAgentTest {

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("fi", "fi-aktia-ob")
                        .setFinancialInstitutionId("aktia")
                        .setAppId("tink")
                        // .addCredentialField(Field.Key.USERNAME, "")
                        // .addCredentialField(Field.Key.PASSWORD, "")
                        .loadCredentialsBefore(true)
                        .saveCredentialsAfter(true)
                        .expectLoggedIn(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
