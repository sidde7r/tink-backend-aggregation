package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

@Ignore
public class DnbAgentTest {

    private final ArgumentManager<DnbAgentTest.Arg> manager =
            new ArgumentManager<>(DnbAgentTest.Arg.values());
    private AgentIntegrationTest.Builder builder;

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void setup() {
        manager.before();
        builder =
                new AgentIntegrationTest.Builder("no", "no-dnb-oauth2")
                        .addCredentialField("PSU-ID", manager.get(Arg.PSU_ID))
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }

    private enum Arg {
        PSU_ID,
    }
}
