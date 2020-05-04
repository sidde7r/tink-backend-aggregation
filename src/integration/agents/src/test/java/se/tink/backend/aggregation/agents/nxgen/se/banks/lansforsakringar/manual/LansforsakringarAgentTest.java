package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.manual;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.ArgumentManagerEnum;
import se.tink.libraries.credentials.service.RefreshableItem;

public class LansforsakringarAgentTest {

    private enum Arg implements ArgumentManagerEnum {
        USERNAME;

        @Override
        public boolean isOptional() {
            return false;
        }
    }

    private final ArgumentManager<Arg> manager = new ArgumentManager<>(Arg.values());

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void setup() {
        manager.before();
    }

    @Test
    public void testLoginAndRefresh() throws Exception {

        new AgentIntegrationTest.Builder("se", "se-lansforsakringar-bankid")
                .addCredentialField(Key.USERNAME, manager.get(Arg.USERNAME))
                .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                .expectLoggedIn(false)
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .build()
                .testRefresh();
    }
}
