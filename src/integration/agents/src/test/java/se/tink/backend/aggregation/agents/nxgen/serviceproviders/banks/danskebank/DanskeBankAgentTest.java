package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.libraries.credentials.service.RefreshableItem;

public class DanskeBankAgentTest {
    private enum Arg {
        USERNAME,
        PASSWORD,
        MARKET // "fi", "dk" or "no"
    }

    private final Map<String, String> providerName =
            new ImmutableMap.Builder<String, String>()
                    .put("fi", "fi-danskebank-codecard")
                    .put("dk", "dk-danskebank-servicecode")
                    .put("no", "no-danskebank-password")
                    .build();

    private AgentIntegrationTest.Builder builder;
    private final ArgumentManager<Arg> manager = new ArgumentManager<>(Arg.values());

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void setup() {
        manager.before();
        final String market = manager.get(Arg.MARKET);
        assert providerName.containsKey(market);

        builder =
                new AgentIntegrationTest.Builder(market, providerName.get(market))
                        .addCredentialField(Field.Key.USERNAME, manager.get(Arg.USERNAME))
                        .addCredentialField(Field.Key.PASSWORD, manager.get(Arg.PASSWORD))
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
