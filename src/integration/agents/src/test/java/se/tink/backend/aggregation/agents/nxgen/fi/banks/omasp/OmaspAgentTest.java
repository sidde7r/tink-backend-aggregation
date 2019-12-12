package se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernamePasswordArgumentEnum;
import se.tink.libraries.credentials.service.RefreshableItem;

public class OmaspAgentTest {
    private final ArgumentManager<UsernamePasswordArgumentEnum> manager =
            new ArgumentManager<>(UsernamePasswordArgumentEnum.values());

    @Before
    public void setUp() throws Exception {
        manager.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("fi", "fi-omasp-codecard")
                .addCredentialField(
                        Key.USERNAME, manager.get(UsernamePasswordArgumentEnum.USERNAME))
                .addCredentialField(
                        Key.PASSWORD, manager.get(UsernamePasswordArgumentEnum.PASSWORD))
                .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                .saveCredentialsAfter(true)
                .loadCredentialsBefore(true)
                .expectLoggedIn(false)
                .build()
                .testRefresh();
    }
}
