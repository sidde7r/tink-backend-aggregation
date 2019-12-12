package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernameArgumentEnum;
import se.tink.libraries.credentials.service.RefreshableItem;

@Ignore
public class MoreGolfMastercardAgentTest {
    private AgentIntegrationTest.Builder builder;
    private final ArgumentManager<UsernameArgumentEnum> manager =
            new ArgumentManager<>(UsernameArgumentEnum.values());

    @Before
    public void setup() {
        manager.before();
        builder =
                new AgentIntegrationTest.Builder("se", "moregolfmastercard-bankid")
                        .addCredentialField(
                                Field.Key.USERNAME, manager.get(UsernameArgumentEnum.USERNAME))
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }
}
