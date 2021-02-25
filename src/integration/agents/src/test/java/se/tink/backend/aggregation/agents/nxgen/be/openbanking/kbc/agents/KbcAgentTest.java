package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.agents;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.IbanArgumentEnum;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.KbcConstants;

public class KbcAgentTest {

    private AgentIntegrationTest.Builder builder;

    private final ArgumentManager<IbanArgumentEnum> ibanManager =
            new ArgumentManager<>(IbanArgumentEnum.values());

    @Before
    public void setup() {
        ibanManager.before();
        builder =
                new AgentIntegrationTest.Builder("be", "be-kbc-ob")
                        .addCredentialField(
                                KbcConstants.CredentialKeys.IBAN,
                                ibanManager.get(IbanArgumentEnum.IBAN))
                        .setFinancialInstitutionId("7802078d8a7049398f9668e5478934ea")
                        .setAppId("tink")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);
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
