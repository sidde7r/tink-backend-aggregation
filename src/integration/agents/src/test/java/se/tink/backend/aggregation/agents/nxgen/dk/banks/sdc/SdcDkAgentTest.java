package se.tink.backend.aggregation.agents.nxgen.dk.banks.sdc.authenticator;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

public class SdcDkAgentTest {

    private enum Arg implements ArgumentManager.ArgumentManagerEnum {
        USERNAME,
        PASSWORD;

        @Override
        public boolean isOptional() {
            return false;
        }
    }

    private final ArgumentManager<SdcDkAgentTest.Arg> manager =
            new ArgumentManager<>(SdcDkAgentTest.Arg.values());

    @Before
    public void setUp() throws Exception {
        manager.before();
    }

    @Test
    public void testRegisterAndRefresh() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("dk", "dk-lansparbank-password")
                        .addCredentialField(
                                Field.Key.USERNAME, manager.get(SdcDkAgentTest.Arg.USERNAME))
                        .addCredentialField(Key.PASSWORD, manager.get(SdcDkAgentTest.Arg.PASSWORD))
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);

        builder.build().testRefresh();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }
}
