package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.LoadBeforeSaveAfterArgumentEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernamePasswordArgumentEnum;

public class CommerzbankAgentTest {
    private final ArgumentManager<UsernamePasswordArgumentEnum> usernamePasswordManager =
            new ArgumentManager<>(UsernamePasswordArgumentEnum.values());
    private final ArgumentManager<LoadBeforeSaveAfterArgumentEnum> loadBeforeSaveAfterManager =
            new ArgumentManager<>(LoadBeforeSaveAfterArgumentEnum.values());

    private AgentIntegrationTest.Builder builder;

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void before() {
        usernamePasswordManager.before();
        loadBeforeSaveAfterManager.before();

        builder =
                new AgentIntegrationTest.Builder("de", "de-commerzbank-password")
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
    public void testLogin() throws Exception {
        builder.addCredentialField(
                        Field.Key.USERNAME,
                        usernamePasswordManager.get(UsernamePasswordArgumentEnum.USERNAME))
                .addCredentialField(
                        Field.Key.PASSWORD,
                        usernamePasswordManager.get(UsernamePasswordArgumentEnum.PASSWORD))
                .build()
                .testRefresh();
    }
}
