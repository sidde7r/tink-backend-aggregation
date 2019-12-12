package se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.IbanArgumentEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.LoadBeforeSaveAfterArgumentEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernameArgumentEnum;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.CredentialKeys;

public class DeutscheBankBEAgentTest {

    private final ArgumentManager<UsernameArgumentEnum> usernameManager =
            new ArgumentManager<>(UsernameArgumentEnum.values());
    private final ArgumentManager<IbanArgumentEnum> ibanManager =
            new ArgumentManager<>(IbanArgumentEnum.values());
    private final ArgumentManager<LoadBeforeSaveAfterArgumentEnum> loadBeforeSaveAfterManager =
            new ArgumentManager<>(LoadBeforeSaveAfterArgumentEnum.values());
    private AgentIntegrationTest.Builder builder;

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void setup() {
        usernameManager.before();
        ibanManager.before();
        loadBeforeSaveAfterManager.before();
        builder =
                new AgentIntegrationTest.Builder("be", "be-deutschebank-ob")
                        .addCredentialField(
                                CredentialKeys.IBAN, ibanManager.get(IbanArgumentEnum.IBAN))
                        .addCredentialField(
                                CredentialKeys.USERNAME,
                                usernameManager.get(UsernameArgumentEnum.USERNAME))
                        .setFinancialInstitutionId("deutschebank-be")
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

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
