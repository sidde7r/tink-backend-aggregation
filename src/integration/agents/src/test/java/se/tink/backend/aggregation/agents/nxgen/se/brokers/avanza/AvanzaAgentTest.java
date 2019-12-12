package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.ArgumentManagerEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernameArgumentEnum;
import se.tink.libraries.credentials.service.RefreshableItem;

public class AvanzaAgentTest {

    private enum Arg implements ArgumentManagerEnum {
        USERNAME; // 12 digit SSN

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

    private final ArgumentManager<UsernameArgumentEnum> manager =
            new ArgumentManager<>(UsernameArgumentEnum.values());

    private AgentIntegrationTest.Builder builder;

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void setup() {
        manager.before();
        builder =
                new AgentIntegrationTest.Builder("se", "se-avanza-bankid")
                        .addCredentialField(
                                Field.Key.USERNAME, manager.get(UsernameArgumentEnum.USERNAME))
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .setFinancialInstitutionId("Avanza")
                        .doLogout(true);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
