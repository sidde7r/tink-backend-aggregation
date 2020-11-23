package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.agent;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.IbanArgumentEnum;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.CredentialKeys;

public class BelfiusPersistedDataAccessorAgentTest {
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
                new AgentIntegrationTest.Builder("be", "be-belfius-ob")
                        .addCredentialField(CredentialKeys.IBAN, manager.get(IbanArgumentEnum.IBAN))
                        .expectLoggedIn(false)
                        .setFinancialInstitutionId("60cdc3bd42aa43a9ab2e6eeef116bebd")
                        .setAppId("tink")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
