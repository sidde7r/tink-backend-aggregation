package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.DualAgentIntegrationTest;

@Ignore
public class RedsysAgentTest {

    private AgentIntegrationTest.Builder builder;

    private String PROVIDER_NAME = "es-redsys-bbva-ob";

    @Test
    public void testRefresh() throws Exception {
        createAgentTest(PROVIDER_NAME).testRefresh();
    }

    private static AgentIntegrationTest createAgentTest(String providerName) {
        return new AgentIntegrationTest.Builder("es", providerName)
                .setFinancialInstitutionId("redsys")
                .setAppId("tink")
                .expectLoggedIn(false)
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .build();
    }

    public static void runDualAgentTest(String providerName, AgentIntegrationTest reAgentTest)
            throws Exception {
        final AgentIntegrationTest obAgentTest = RedsysAgentTest.createAgentTest(providerName);
        DualAgentIntegrationTest.of(reAgentTest, obAgentTest).testAndCompare();
    }
}
