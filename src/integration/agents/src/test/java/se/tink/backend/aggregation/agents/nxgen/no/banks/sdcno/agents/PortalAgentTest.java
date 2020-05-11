package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.agents;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

public class PortalAgentTest {

    private enum Arg implements ArgumentManager.ArgumentManagerEnum {
        MOBILE_NUMBER,
        NATIONAL_ID_NUMBER;

        @Override
        public boolean isOptional() {
            return false;
        }
    }

    private final ArgumentManager<PortalAgentTest.Arg> manager =
            new ArgumentManager<>(PortalAgentTest.Arg.values());

    @Before
    public void setUp() throws Exception {
        manager.before();
    }

    @Test
    public void testRegisterAndRefresh() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("no", "no-indre-sogn-sparebank")
                        .addCredentialField(
                                Field.Key.MOBILENUMBER,
                                manager.get(PortalAgentTest.Arg.MOBILE_NUMBER))
                        .addCredentialField(
                                Field.Key.NATIONAL_ID_NUMBER,
                                manager.get(PortalAgentTest.Arg.NATIONAL_ID_NUMBER))
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
