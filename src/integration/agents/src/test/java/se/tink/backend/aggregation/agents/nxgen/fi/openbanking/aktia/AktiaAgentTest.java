package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.IbanArgumentEnum;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaConstants.CredentialKeys;

@Ignore
public class AktiaAgentTest {
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
                new AgentIntegrationTest.Builder("fi", "fi-aktia-oauth2")
                        .addCredentialField(CredentialKeys.IBAN, manager.get(IbanArgumentEnum.IBAN))
                        .expectLoggedIn(false)
                        .setAppId("tink")
                        .setFinancialInstitutionId("aktia")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
