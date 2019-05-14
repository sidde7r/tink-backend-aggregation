package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

public class ArgentaAgentTest {
    private enum Arg {
        LOAD_BEFORE,
        SAVE_AFTER,

        /*
        The card number contains spaces, so you have to URL-encode it like so:

        --jvmopt=-Dtink.CARD_NUMBER=6703%20xxxx%20xxxx%20xxxx%20x
        --jvmopt=-Dtink.urlencoded
        */
        CARD_NUMBER,
    }

    private final ArgumentManager<Arg> helper = new ArgumentManager<>(Arg.values());

    @Before
    public void before() {
        helper.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    // You will need card/ Argenta digipass and the service for supplementary information
    // Argenta allows a maximum of 10 devices, please remove not used devices by web interface after
    // testing
    @Test
    public void testRefresh() throws Exception {
        final AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("be", "be-argenta-cardreader")
                        .addCredentialField(Field.Key.USERNAME, helper.get(Arg.CARD_NUMBER))
                        .loadCredentialsBefore(Boolean.parseBoolean(helper.get(Arg.LOAD_BEFORE)))
                        .saveCredentialsAfter(Boolean.parseBoolean(helper.get(Arg.SAVE_AFTER)));

        builder.build().testRefresh();
    }
}
