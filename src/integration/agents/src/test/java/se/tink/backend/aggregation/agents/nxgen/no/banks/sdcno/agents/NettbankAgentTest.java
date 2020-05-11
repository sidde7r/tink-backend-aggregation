package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.agents;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

public class NettbankAgentTest {

    private enum Arg implements ArgumentManager.ArgumentManagerEnum {
        MOBILE_NUMBER,
        DATE_OF_BIRTH;

        @Override
        public boolean isOptional() {
            return false;
        }
    }

    private final ArgumentManager<NettbankAgentTest.Arg> manager =
            new ArgumentManager<>(NettbankAgentTest.Arg.values());

    @Before
    public void setUp() throws Exception {
        manager.before();
    }

    @Test
    public void testRegisterAndRefresh() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("no", "no-cultura-bank")
                        .addCredentialField(
                                Field.Key.MOBILENUMBER,
                                manager.get(NettbankAgentTest.Arg.MOBILE_NUMBER))
                        .addCredentialField(
                                Field.Key.DATE_OF_BIRTH,
                                manager.get(NettbankAgentTest.Arg.DATE_OF_BIRTH))
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(true)
                        .expectLoggedIn(false);

        builder.build().testRefresh();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }
}
