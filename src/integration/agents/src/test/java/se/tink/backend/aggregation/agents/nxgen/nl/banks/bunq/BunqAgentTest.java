package se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.ArgumentManagerEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.PasswordArgumentEnum;

public class BunqAgentTest {
    private final ArgumentManager<Arg> manager = new ArgumentManager<>(Arg.values());
    private final ArgumentManager<PasswordArgumentEnum> passwordManager =
            new ArgumentManager<>(PasswordArgumentEnum.values());

    private AgentIntegrationTest.Builder builder;

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void before() {
        manager.before();
        passwordManager.before();

        builder =
                new AgentIntegrationTest.Builder("nl", "nl-bunq-sandbox-apikey")
                        .loadCredentialsBefore(Boolean.parseBoolean(manager.get(Arg.LOAD_BEFORE)))
                        .saveCredentialsAfter(Boolean.parseBoolean(manager.get(Arg.SAVE_AFTER)));
    }

    @Test
    public void testLogin() throws Exception {
        builder.addCredentialField(Key.PASSWORD, passwordManager.get(PasswordArgumentEnum.PASSWORD))
                .build()
                .testRefresh();
    }

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
}
