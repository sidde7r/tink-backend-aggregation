package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.manual;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.ArgumentManagerEnum;

public final class VolksbankAgentTest {
    private enum Arg implements ArgumentManagerEnum {
        LOAD_BEFORE,
        SAVE_AFTER;

        private final boolean optional;

        Arg(boolean optional) {
            this.optional = optional;
        }

        Arg() {
            this.optional = false;
        }

        @Override
        public boolean isOptional() {
            return optional;
        }
    }

    private final ArgumentManager<Arg> manager = new ArgumentManager<>(Arg.values());

    @Before
    public void before() {
        manager.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    private AgentIntegrationTest createAgentTest(final String providerName) {
        return new AgentIntegrationTest.Builder("nl", providerName)
                .loadCredentialsBefore(Boolean.parseBoolean(manager.get(Arg.LOAD_BEFORE)))
                .saveCredentialsAfter(Boolean.parseBoolean(manager.get(Arg.SAVE_AFTER)))
                .setFinancialInstitutionId("volksbank")
                .setAppId("tink")
                .expectLoggedIn(false)
                .build();
    }

    @Test
    public void testSnsBank() throws Exception {
        createAgentTest("nl-snsbank-oauth2").testRefresh();
    }

    @Test
    public void testAsnBank() throws Exception {
        createAgentTest("nl-asnbank-oauth2").testRefresh();
    }

    @Test
    public void testRegioBank() throws Exception {
        createAgentTest("nl-regiobank-oauth2").testRefresh();
    }

    @Test
    public void testSnsBankSandbox() throws Exception {
        createAgentTest("nl-snsbank-sandbox-oauth2").testRefresh();
    }
}
