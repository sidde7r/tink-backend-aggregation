package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

public final class RabobankAgentTest {
    private enum Arg {
        LOAD_BEFORE,
        SAVE_AFTER,
        IS_MANUAL,
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

    @Test
    public void refreshSandbox() throws Exception {
        new AgentIntegrationTest.Builder("nl", "nl-rabobank-sandbox-oauth2")
                .loadCredentialsBefore(Boolean.parseBoolean(manager.get(Arg.LOAD_BEFORE)))
                .saveCredentialsAfter(Boolean.parseBoolean(manager.get(Arg.SAVE_AFTER)))
                .setRequestFlagManual(Boolean.parseBoolean(manager.get(Arg.IS_MANUAL)))
                .expectLoggedIn(false)
                .build()
                .testRefresh();
    }

    @Test
    public void refreshProduction() throws Exception {
        new AgentIntegrationTest.Builder("nl", "nl-rabobank-oauth2")
                .loadCredentialsBefore(Boolean.parseBoolean(manager.get(Arg.LOAD_BEFORE)))
                .saveCredentialsAfter(Boolean.parseBoolean(manager.get(Arg.SAVE_AFTER)))
                .setRequestFlagManual(Boolean.parseBoolean(manager.get(Arg.IS_MANUAL)))
                .expectLoggedIn(false)
                .build()
                .testRefresh();
    }

    @Test
    public void refreshProductionAbnAmro() throws Exception {
        new AgentIntegrationTest.Builder("nl", "nl-rabobank-abn-oauth2")
                .loadCredentialsBefore(Boolean.parseBoolean(manager.get(Arg.LOAD_BEFORE)))
                .saveCredentialsAfter(Boolean.parseBoolean(manager.get(Arg.SAVE_AFTER)))
                .setRequestFlagManual(Boolean.parseBoolean(manager.get(Arg.IS_MANUAL)))
                .expectLoggedIn(false)
                .build()
                .testRefresh();
    }
}
