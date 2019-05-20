package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

public class DanskeBankAgentTest {
    private enum Arg {
        USERNAME,
        PASSWORD,
        MARKET // "fi" or "dk"
    }

    private final Map<String, String> providerName =
            new ImmutableMap.Builder<String, String>()
                    .put("fi", "fi-danskebank-codecard")
                    .put("dk", "dk-danskebank-servicecode")
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
                        .loadCredentialsBefore(true)
                        .saveCredentialsAfter(true);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
