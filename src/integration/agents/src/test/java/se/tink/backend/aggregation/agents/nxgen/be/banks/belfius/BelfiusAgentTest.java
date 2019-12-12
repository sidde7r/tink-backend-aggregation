package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.ArgumentManagerEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernamePasswordArgumentEnum;

public class BelfiusAgentTest {
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

    private final ArgumentManager<Arg> helper = new ArgumentManager<>(Arg.values());
    private final ArgumentManager<UsernamePasswordArgumentEnum> usernamePasswordHelper =
            new ArgumentManager<>(UsernamePasswordArgumentEnum.values());

    @Before
    public void before() {
        helper.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testRefresh() throws Exception {
        buildWithCredentials().testRefresh();
    }

    private AgentIntegrationTest buildWithCredentials() {
        return new AgentIntegrationTest.Builder("be", "be-belfius-cardreader")
                .loadCredentialsBefore(Boolean.parseBoolean(helper.get(Arg.LOAD_BEFORE)))
                .saveCredentialsAfter(Boolean.parseBoolean(helper.get(Arg.SAVE_AFTER)))
                .addCredentialField(
                        Field.Key.USERNAME,
                        usernamePasswordHelper.get(UsernamePasswordArgumentEnum.USERNAME))
                .addCredentialField(
                        Field.Key.PASSWORD,
                        usernamePasswordHelper.get(UsernamePasswordArgumentEnum.PASSWORD))
                .build();
    }
}
