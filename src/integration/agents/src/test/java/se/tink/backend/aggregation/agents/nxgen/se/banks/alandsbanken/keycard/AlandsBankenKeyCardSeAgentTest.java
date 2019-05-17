package se.tink.backend.aggregation.agents.nxgen.se.banks.alandsbanken.keycard;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

@Ignore
public class AlandsBankenKeyCardSeAgentTest {
    private enum Arg {
        USERNAME,
        PASSWORD
    }

    private final ArgumentManager<Arg> helper = new ArgumentManager<>(Arg.values());

    private final AgentIntegrationTest.Builder builder =
            new AgentIntegrationTest.Builder("se", "alandsbanken")
                    .loadCredentialsBefore(false)
                    .saveCredentialsAfter(false);

    @Before
    public void setup() {
        helper.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testRefresh() throws Exception {
        builder.addCredentialField(Field.Key.USERNAME, helper.get(Arg.USERNAME))
                .addCredentialField(Field.Key.PASSWORD, helper.get(Arg.PASSWORD))
                .build()
                .testRefresh();
    }
}
