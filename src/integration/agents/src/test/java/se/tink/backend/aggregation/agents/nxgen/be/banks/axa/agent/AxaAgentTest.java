package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.agent;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.ArgumentManagerEnum;

public class AxaAgentTest {
    private enum Arg implements ArgumentManagerEnum {
        LOAD_BEFORE,
        SAVE_AFTER,
        CARD_NUMBER;

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

    private AgentIntegrationTest.Builder testBuilder;

    @Before
    public void before() {
        manager.before();

        testBuilder =
                new AgentIntegrationTest.Builder("be", "be-axa-cardreader")
                        .expectLoggedIn(false)
                        .addCredentialField(Field.Key.USERNAME, manager.get(Arg.CARD_NUMBER))
                        .loadCredentialsBefore(Boolean.parseBoolean(manager.get(Arg.LOAD_BEFORE)))
                        .saveCredentialsAfter(Boolean.parseBoolean(manager.get(Arg.SAVE_AFTER)));
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testLoginAndRefresh() throws Exception {
        testBuilder.build().testRefresh();
    }

    @Test
    public void testLoginAndRefreshWithBelgianLocale() throws Exception {
        testBuilder.setUserLocale("nl_BE").build().testRefresh();
    }

    @Test
    public void testLoginAndRefreshWithGermanLocale() throws Exception {
        testBuilder.setUserLocale("de_BE").build().testRefresh();
    }

    @Test
    public void testLoginAndRefreshWithFrenchLocale() throws Exception {
        testBuilder.setUserLocale("fr_BE").build().testRefresh();
    }

    @Test
    public void testLoginAndRefreshWithEnglishLocale() throws Exception {
        testBuilder.setUserLocale("en_GB").build().testRefresh();
    }
}
