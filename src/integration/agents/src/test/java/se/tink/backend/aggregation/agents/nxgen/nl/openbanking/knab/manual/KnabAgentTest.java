package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.manual;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.IbanArgumentEnum;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.KnabConstants.CredentialKeys;

public class KnabAgentTest {

    private final ArgumentManager<IbanArgumentEnum> manager =
            new ArgumentManager<>(IbanArgumentEnum.values());
    private AgentIntegrationTest.Builder builder;

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void setup() {
        manager.before();
        builder =
                new AgentIntegrationTest.Builder("nl", "nl-knab-ob")
                        .addCredentialField(
                                CredentialKeys.IBANS, manager.get(IbanArgumentEnum.IBAN))
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .setFinancialInstitutionId("knab")
                        .setAppId("tink")
                        .expectLoggedIn(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
