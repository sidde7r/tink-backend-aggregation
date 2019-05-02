package se.tink.backend.aggregation.agents.nxgen.se.creditcards.amex;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

public final class AmericanExpressSEV62AgentTest {
    private enum Arg {
        USERNAME,
        PASSWORD,
        PROVIDER, // "americanexpress" or "saseurobonusamericanexpress"
    }

    private final ArgumentManager<Arg> helper = new ArgumentManager<>(Arg.values());

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        helper.before();

        builder =
                new AgentIntegrationTest.Builder("se", helper.get(Arg.PROVIDER))
                        .addCredentialField(Field.Key.USERNAME, helper.get(Arg.USERNAME))
                        .addCredentialField(Field.Key.PASSWORD, helper.get(Arg.PASSWORD))
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .doLogout(true);
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
