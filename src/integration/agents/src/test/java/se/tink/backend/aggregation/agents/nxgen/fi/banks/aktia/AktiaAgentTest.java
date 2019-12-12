package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernamePasswordArgumentEnum;
import se.tink.libraries.credentials.service.RefreshableItem;

public class AktiaAgentTest {
    private final ArgumentManager<UsernamePasswordArgumentEnum> helper =
            new ArgumentManager<>(UsernamePasswordArgumentEnum.values());

    private final AgentIntegrationTest.Builder builder =
            new AgentIntegrationTest.Builder("fi", "fi-aktia-codecard")
                    .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                    .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                    .doLogout(false)
                    .expectLoggedIn(false)
                    .loadCredentialsBefore(true)
                    .saveCredentialsAfter(true);

    @Before
    public void before() {
        helper.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testLoginAndRefresh() throws Exception {
        builder.addCredentialField(
                        Field.Key.USERNAME, helper.get(UsernamePasswordArgumentEnum.USERNAME))
                .addCredentialField(
                        Field.Key.PASSWORD, helper.get(UsernamePasswordArgumentEnum.PASSWORD))
                .build()
                .testRefresh();
    }
}
