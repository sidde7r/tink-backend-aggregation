package se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.IbanArgumentEnum;

public class RaiffeisenAgentTest {

    private AgentIntegrationTest.Builder builder;
    private final ArgumentManager<IbanArgumentEnum> manager =
            new ArgumentManager<>(IbanArgumentEnum.values());

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void setup() {
        manager.before();

        builder =
                new AgentIntegrationTest.Builder("at", "at-raiffeisen-ob")
                        .addCredentialField(
                                RaiffeisenConstants.CredentialKeys.IBAN,
                                manager.get(IbanArgumentEnum.IBAN))
                        .setFinancialInstitutionId("fe0c6d6af42a4423b08ee0a8019e01e4")
                        .setAppId("tink")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(true)
                        .saveCredentialsAfter(true);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
