package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank;

import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.libraries.credentials.service.RefreshableItem;

public class DemobankAgentTest {
    private static final String USERNAME = "u0001";
    private static final String PASSWORD = "abc123";

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        Set<RefreshableItem> items = new HashSet<>();
        items.addAll(RefreshableItem.REFRESHABLE_ITEMS_ALL);
        items.add(RefreshableItem.IDENTITY_DATA);

        builder =
                new AgentIntegrationTest.Builder("uk", "uk-demobank-password")
                        .addCredentialField(Field.Key.USERNAME, USERNAME)
                        .addCredentialField(Field.Key.PASSWORD, PASSWORD)
                        .loadCredentialsBefore(false)
                        .setRefreshableItems(items)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
