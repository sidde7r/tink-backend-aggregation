package se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.IbanArgumentEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.LoadBeforeSaveAfterArgumentEnum;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.ErstebankConstants.CredentialKeys;
import se.tink.libraries.credentials.service.RefreshableItem;

public class ErstebankAgentTest {

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
                new AgentIntegrationTest.Builder("at", "at-erstebank-ob")
                        .addCredentialField(
                                CredentialKeys.IBAN, ibanManager.get(IbanArgumentEnum.IBAN))
                        .setFinancialInstitutionId("c0fb80b608514df4bc803c41da582097")
                        .setAppId("tink")
                        .loadCredentialsBefore(
                                Boolean.parseBoolean(
                                        loadBeforeSaveAfterManager.get(
                                                LoadBeforeSaveAfterArgumentEnum.LOAD_BEFORE)))
                        .saveCredentialsAfter(
                                Boolean.parseBoolean(
                                        loadBeforeSaveAfterManager.get(
                                                LoadBeforeSaveAfterArgumentEnum.SAVE_AFTER)))
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .expectLoggedIn(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
