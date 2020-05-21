package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.agents;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.IbanArgumentEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.LoadBeforeSaveAfterArgumentEnum;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.KbcConstants;

public class KbcAgentTest {

    private AgentIntegrationTest.Builder builder;

    private final ArgumentManager<LoadBeforeSaveAfterArgumentEnum> loadBeforeSaveAfterManager =
            new ArgumentManager<>(LoadBeforeSaveAfterArgumentEnum.values());
    private final ArgumentManager<IbanArgumentEnum> ibanManager =
            new ArgumentManager<>(IbanArgumentEnum.values());

    @Before
    public void setup() {
        loadBeforeSaveAfterManager.before();
        ibanManager.before();
        builder =
                new AgentIntegrationTest.Builder("be", "be-kbc-ob")
                        .addCredentialField(
                                KbcConstants.CredentialKeys.IBAN,
                                ibanManager.get(IbanArgumentEnum.IBAN))
                        .setFinancialInstitutionId("7802078d8a7049398f9668e5478934ea")
                        .setAppId("tink")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(
                                Boolean.parseBoolean(
                                        loadBeforeSaveAfterManager.get(
                                                LoadBeforeSaveAfterArgumentEnum.LOAD_BEFORE)))
                        .saveCredentialsAfter(
                                Boolean.parseBoolean(
                                        loadBeforeSaveAfterManager.get(
                                                LoadBeforeSaveAfterArgumentEnum.SAVE_AFTER)));
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
