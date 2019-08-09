package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.DualAgentIntegrationTest;
import se.tink.libraries.credentials.service.RefreshableItem;

public class NordeaDualAgentIntegrationTest {

    private enum Arg {
        USERNAME, // 12 digit SSN
    }

    private final ArgumentManager<Arg> manager = new ArgumentManager<>(Arg.values());

    @Before
    public void setup() {
        manager.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void dualTest() throws Exception {
        DualAgentIntegrationTest.of(
                        new AgentIntegrationTest.Builder("se", "se-nordea-ob")
                                .addCredentialField(Field.Key.USERNAME, manager.get(Arg.USERNAME))
                                .expectLoggedIn(false)
                                .loadCredentialsBefore(false)
                                .saveCredentialsAfter(true)
                                .build(),
                        new AgentIntegrationTest.Builder("se", "se-nordea-bankid")
                                .loadCredentialsBefore(false)
                                .saveCredentialsAfter(false)
                                .doLogout(true)
                                .addCredentialField(Field.Key.USERNAME, manager.get(Arg.USERNAME))
                                .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                                .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                                .build())
                .testAndCompare();
    }
}
