package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.libraries.credentials.service.RefreshableItem;

public class NordeaSEAgentPasswordTest {
    private AgentIntegrationTest.Builder builder =
            new AgentIntegrationTest.Builder("se", "se-nordea-password")
                    .loadCredentialsBefore(true)
                    .saveCredentialsAfter(true)
                    .doLogout(true);

    private enum Arg {
        USERNAME, // 12 digit SSN
        PASSWORD, // 4 digit personal code
    }

    private final ArgumentManager<NordeaSEAgentPasswordTest.Arg> manager =
            new ArgumentManager<>(NordeaSEAgentPasswordTest.Arg.values());

    @Before
    public void setup() {
        manager.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testRefresh() throws Exception {
        builder.addCredentialField(Field.Key.USERNAME, manager.get(Arg.USERNAME))
                .addCredentialField(Key.PASSWORD, manager.get(Arg.PASSWORD))
                .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                .build()
                .testRefresh();
    }
}
