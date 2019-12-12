package se.tink.backend.aggregation.agents.banks.norwegian;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernameArgumentEnum;
import se.tink.libraries.credentials.service.RefreshableItem;

public class NorwegianAgentTest {
    private final ArgumentManager<UsernameArgumentEnum> manager =
            new ArgumentManager<>(UsernameArgumentEnum.values());

    @Before
    public void setUp() throws Exception {
        manager.before();
    }

    @Test
    public void testLogin() throws Exception {
        new AgentIntegrationTest.Builder("se", "norwegian-bankid")
                .addCredentialField(Field.Key.USERNAME, manager.get(UsernameArgumentEnum.USERNAME))
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                .build()
                .testRefresh();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }
}
