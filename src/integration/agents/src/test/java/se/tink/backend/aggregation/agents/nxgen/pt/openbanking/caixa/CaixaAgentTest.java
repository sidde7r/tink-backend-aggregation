package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.caixa;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.CredentialKeys;

@Ignore
public class CaixaAgentTest {

    private AgentIntegrationTest.Builder builder;

    private final String TEST_PSU_ID_TYPE = "CP";
    private final String TEST_PSU_ID = "3273572";
    // private final String TEST_PSU_ID = "6362699";
    // private final String TEST_PSU_ID = "3140331";

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("pt", "pt-caixa-oauth2")
                        .addCredentialField(CredentialKeys.PSU_ID_TYPE, TEST_PSU_ID_TYPE)
                        .addCredentialField(CredentialKeys.PSU_ID, TEST_PSU_ID)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
