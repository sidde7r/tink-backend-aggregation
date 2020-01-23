package se.tink.backend.aggregation.agents.nxgen.de.banks.fints;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.ArgumentManagerEnum;

@Ignore
public final class FinTsAgentTest {

    private enum Arg implements ArgumentManagerEnum {
        USERNAME,
        PASSWORD,
        MARKET,
        PROVIDER;

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

    private final ArgumentManager<Arg> helper = new ArgumentManager<>(Arg.values());

    @Before
    public void before() {
        helper.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void refresh() throws Exception {
        new AgentIntegrationTest.Builder(helper.get(Arg.MARKET), helper.get(Arg.PROVIDER))
                .addCredentialField(Field.Key.USERNAME, helper.get(Arg.USERNAME))
                .addCredentialField(Field.Key.PASSWORD, helper.get(Arg.PASSWORD))
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .build()
                .testRefresh();
    }
}
