package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.ArgumentManagerEnum;
import se.tink.backend.aggregation.agents.framework.DualAgentIntegrationTest;

public class RedsysAgentTest {
    private final ArgumentManager<ProviderNameEnum> manager =
            new ArgumentManager<>(ProviderNameEnum.values());

    public enum ProviderNameEnum implements ArgumentManagerEnum {
        PROVIDER_NAME;

        @Override
        public boolean isOptional() {
            return false;
        }
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void setup() {
        manager.before();
    }

    @Test
    public void testRefresh() throws Exception {
        createAgentTest(manager.get(ProviderNameEnum.PROVIDER_NAME)).testRefresh();
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
