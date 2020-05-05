package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.manual;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.ArgumentManagerEnum;
import se.tink.libraries.credentials.service.RefreshableItem;

public class SkandiaBankenAgentTest {
    private enum Arg implements ArgumentManagerEnum {
        SSN(),
        CREDENTIAL_NAME(true);

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
                new AgentIntegrationTest.Builder("se", "se-skandiabanken-bankid")
                        .addCredentialField(Field.Key.USERNAME, manager.get(Arg.SSN))
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .setFinancialInstitutionId("skandiabanken")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testLoginAndRefresh() throws Exception {
        testBuilder.build().testRefresh();
    }
}
