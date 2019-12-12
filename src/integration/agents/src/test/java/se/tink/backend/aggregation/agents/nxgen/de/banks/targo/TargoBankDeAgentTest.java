package se.tink.backend.aggregation.agents.nxgen.de.banks.targo;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.LoadBeforeSaveAfterArgumentEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernamePasswordArgumentEnum;
import se.tink.libraries.credentials.service.RefreshableItem;

public final class TargoBankDeAgentTest {
    private final ArgumentManager<LoadBeforeSaveAfterArgumentEnum> loadBeforeSaveAfterManager =
            new ArgumentManager<>(LoadBeforeSaveAfterArgumentEnum.values());
    private final ArgumentManager<UsernamePasswordArgumentEnum> usernamePasswordManager =
            new ArgumentManager<>(UsernamePasswordArgumentEnum.values());

    @Before
    public void before() {
        loadBeforeSaveAfterManager.before();
        usernamePasswordManager.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    private AgentIntegrationTest.Builder builder() {
        return new AgentIntegrationTest.Builder("de", "de-targobank-password")
                .loadCredentialsBefore(
                        Boolean.parseBoolean(
                                loadBeforeSaveAfterManager.get(
                                        LoadBeforeSaveAfterArgumentEnum.LOAD_BEFORE)))
                .saveCredentialsAfter(
                        Boolean.parseBoolean(
                                loadBeforeSaveAfterManager.get(
                                        LoadBeforeSaveAfterArgumentEnum.SAVE_AFTER)))
                .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                .addRefreshableItems(RefreshableItem.IDENTITY_DATA);
    }

    @Test
    public void testRefresh() throws Exception {
        builder()
                .addCredentialField(
                        Field.Key.USERNAME,
                        usernamePasswordManager.get(UsernamePasswordArgumentEnum.USERNAME))
                .addCredentialField(
                        Field.Key.PASSWORD,
                        usernamePasswordManager.get(UsernamePasswordArgumentEnum.PASSWORD))
                .build()
                .testRefresh();
    }
}
