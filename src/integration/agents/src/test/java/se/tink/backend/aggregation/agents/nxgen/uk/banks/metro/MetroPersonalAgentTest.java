package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.SecurityNumberArgumentEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernamePasswordArgumentEnum;
import se.tink.libraries.credentials.service.RefreshableItem;

public class MetroPersonalAgentTest {
    private final ArgumentManager<UsernamePasswordArgumentEnum> usernamePasswordManager =
            new ArgumentManager<>(UsernamePasswordArgumentEnum.values());
    private final ArgumentManager<SecurityNumberArgumentEnum> secureNumberManager =
            new ArgumentManager<>(SecurityNumberArgumentEnum.values());

    @Before
    public void setUp() {
        usernamePasswordManager.before();
        secureNumberManager.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-metro-personal-password")
                .addCredentialField(
                        Field.Key.USERNAME,
                        usernamePasswordManager.get(UsernamePasswordArgumentEnum.USERNAME))
                .addCredentialField(
                        Field.Key.PASSWORD,
                        usernamePasswordManager.get(UsernamePasswordArgumentEnum.PASSWORD))
                .addCredentialField(
                        Field.Key.SECURITY_NUMBER,
                        secureNumberManager.get(SecurityNumberArgumentEnum.SECURITY_NUMBER))
                .loadCredentialsBefore(true)
                .saveCredentialsAfter(true)
                .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                .build()
                .testRefresh();
    }
}
