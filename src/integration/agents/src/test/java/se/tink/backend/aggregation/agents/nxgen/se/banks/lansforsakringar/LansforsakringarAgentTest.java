package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar;

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

    // The bank credentials are added to the run configuration.
    // This removes the risk of accidentally committing sensitive information.u
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
                // Can be anything for now but has to match prod value for real testing
                .setFinancialInstitutionId("lansforshakringar")
                // Device pinning (we already have saved credentials
                .loadCredentialsBefore(false)
                // Save to either db in prod or local storage for testing
                .saveCredentialsAfter(false)
                .build()
                .testRefresh();
    }
}
