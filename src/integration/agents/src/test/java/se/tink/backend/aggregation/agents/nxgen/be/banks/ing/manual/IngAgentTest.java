package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.manual;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.ArgumentManagerEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernameArgumentEnum;

public class IngAgentTest {

    private enum Arg implements ArgumentManagerEnum {
        CARD_ID;

        @Override
        public boolean isOptional() {
            return false;
        }
    }

    private final ArgumentManager<Arg> cardIdHelper = new ArgumentManager<>(Arg.values());
    private final ArgumentManager<UsernameArgumentEnum> usernameHelper =
            new ArgumentManager<>(UsernameArgumentEnum.values());

    @Before
    public void before() {
        cardIdHelper.before();
        usernameHelper.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("be", "be-ing-cardreader")
                .addCredentialField(
                        Field.Key.USERNAME, usernameHelper.get(UsernameArgumentEnum.USERNAME))
                .addCredentialField("cardId", cardIdHelper.get(Arg.CARD_ID))
                .loadCredentialsBefore(true)
                .saveCredentialsAfter(true)
                .build()
                .testRefresh();
    }
}
