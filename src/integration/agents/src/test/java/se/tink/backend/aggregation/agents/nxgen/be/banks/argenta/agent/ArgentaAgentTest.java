package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.agent;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.ArgumentManagerEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernameArgumentEnum;

public class ArgentaAgentTest {
    private enum Arg implements ArgumentManagerEnum {
        LOAD_BEFORE,
        SAVE_AFTER;
        private final boolean optional;

        Arg() {
            this.optional = false;
        }

        @Override
        public boolean isOptional() {
            return optional;
        }
    }

    private final ArgumentManager<Arg> helper = new ArgumentManager<>(Arg.values());
    private final ArgumentManager<UsernameArgumentEnum> usernameHelper =
            new ArgumentManager<>(UsernameArgumentEnum.values());

    @Before
    public void before() {
        helper.before();
        usernameHelper.before();
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
                        /*
                        The card number contains spaces, so you have to URL-encode it like so:

                        --jvmopt=-Dtink.USERNAME=6703%20xxxx%20xxxx%20xxxx%20x
                        --jvmopt=-Dtink.urlencoded
                        */
                        .addCredentialField(
                                "username", usernameHelper.get(UsernameArgumentEnum.USERNAME))
                        .loadCredentialsBefore(Boolean.parseBoolean(helper.get(Arg.LOAD_BEFORE)))
                        .saveCredentialsAfter(Boolean.parseBoolean(helper.get(Arg.SAVE_AFTER)));

        builder.build().testRefresh();
    }
}
