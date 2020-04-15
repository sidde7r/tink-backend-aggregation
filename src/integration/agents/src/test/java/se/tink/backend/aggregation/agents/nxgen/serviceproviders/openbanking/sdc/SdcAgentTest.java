package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class SdcAgentTest {

    private static final String TEST_SSN = "";

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("dk", "dk-banknordik-ob")
                        .addCredentialField(Key.LOGIN_INPUT, TEST_SSN)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false)
                        .setAppId("tink")
                        .setFinancialInstitutionId("banknordik");
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
