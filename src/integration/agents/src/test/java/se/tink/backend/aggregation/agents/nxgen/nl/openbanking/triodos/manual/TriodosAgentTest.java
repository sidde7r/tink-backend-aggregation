package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.manual;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.IbanArgumentEnum;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.TriodosConstants.CredentialKeys;

public class TriodosAgentTest {

    private AgentIntegrationTest.Builder builder;
    private final ArgumentManager<IbanArgumentEnum> manager =
            new ArgumentManager<>(IbanArgumentEnum.values());

    @Before
    public void setup() {
        manager.before();
        builder =
                new AgentIntegrationTest.Builder("nl", "nl-triodos-ob")
                        .addCredentialField(
                                CredentialKeys.IBANS, manager.get(IbanArgumentEnum.IBAN))
                        .setFinancialInstitutionId("triodos")
                        .setAppId("tink")
                        .setClusterId("oxford-preprod")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false);
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
