package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.libraries.credentials.service.RefreshableItem;

public class JyskeAgentTest {

    private final ArgumentManager<JyskeAgentTest.Arg> manager =
            new ArgumentManager<>(JyskeAgentTest.Arg.values());

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void setup() {
        manager.before();
    }

    @Test
    public void refresh() throws Exception {
        new AgentIntegrationTest.Builder("dk", "dk-jyskebank-codecard")
                .addCredentialField(Field.Key.USERNAME, manager.get(JyskeAgentTest.Arg.USERNAME))
                .addCredentialField(Field.Key.PASSWORD, manager.get(JyskeAgentTest.Arg.PASSWORD))
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                .expectLoggedIn(false)
                .build()
                .testRefresh();
    }

    private enum Arg {
        USERNAME,
        PASSWORD
    }
}
