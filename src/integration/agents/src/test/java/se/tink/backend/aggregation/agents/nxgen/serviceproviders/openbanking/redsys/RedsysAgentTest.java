package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class RedsysAgentTest {

    private AgentIntegrationTest.Builder builder;

    private String PROVIDER_NAME = "es-redsys-bbva-ob";

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("es", PROVIDER_NAME)
                        .setFinancialInstitutionId("redsys")
                        .setAppId("tink")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
