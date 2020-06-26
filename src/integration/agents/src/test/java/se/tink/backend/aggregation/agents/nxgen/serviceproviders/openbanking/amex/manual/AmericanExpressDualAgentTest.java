package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.manual;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.ArgumentManagerEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernamePasswordArgumentEnum;
import se.tink.backend.aggregation.agents.framework.DualAgentIntegrationTest;

public final class AmericanExpressDualAgentTest {
    private enum Arg implements ArgumentManagerEnum {
        PROVIDER; // "americanexpress" or "saseurobonusamericanexpress"

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
    private final ArgumentManager<UsernamePasswordArgumentEnum> credentialHelper =
            new ArgumentManager<>(UsernamePasswordArgumentEnum.values());

    private AgentIntegrationTest.Builder reBuilder;
    private AgentIntegrationTest.Builder obBuilder;

    @Before
    public void setup() {
        helper.before();
        credentialHelper.before();
        obBuilder =
                new AgentIntegrationTest.Builder("se", "se-amex-ob")
                        .setFinancialInstitutionId("amex")
                        .setAppId("tink")
                        .loadCredentialsBefore(true)
                        .saveCredentialsAfter(true)
                        .expectLoggedIn(false);

        reBuilder =
                new AgentIntegrationTest.Builder("se", helper.get(Arg.PROVIDER))
                        .addCredentialField(
                                Field.Key.USERNAME,
                                credentialHelper.get(UsernamePasswordArgumentEnum.USERNAME))
                        .addCredentialField(
                                Field.Key.PASSWORD,
                                credentialHelper.get(UsernamePasswordArgumentEnum.PASSWORD))
                        .loadCredentialsBefore(true)
                        .saveCredentialsAfter(true)
                        .doLogout(true)
                        .expectLoggedIn(false);
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testRefresh() throws Exception {
        final DualAgentIntegrationTest test =
                DualAgentIntegrationTest.of(obBuilder.build(), reBuilder.build());
        test.testAndCompare();
    }
}
