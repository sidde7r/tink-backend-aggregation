package se.tink.backend.aggregation.agents.nxgen.no.banks.sdc;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

public class SdcNoScreenScrapingIFrameTest {

    private enum Arg implements ArgumentManager.ArgumentManagerEnum {
        USERNAME,
        PASSWORD;

        @Override
        public boolean isOptional() {
            return false;
        }
    }

    private final ArgumentManager<SdcNoScreenScrapingIFrameTest.Arg> manager =
            new ArgumentManager<>(SdcNoScreenScrapingIFrameTest.Arg.values());

    @Before
    public void setUp() throws Exception {
        manager.before();
    }

    @Test
    public void testRegisterAndRefresh() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("no", "no-cultura-bank")
                        .addCredentialField(
                                Field.Key.USERNAME,
                                manager.get(SdcNoScreenScrapingIFrameTest.Arg.USERNAME))
                        .addCredentialField(
                                Field.Key.PASSWORD,
                                manager.get(SdcNoScreenScrapingIFrameTest.Arg.PASSWORD))
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
