package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.libraries.credentials.service.RefreshableItem;

public class IngAgentTest {
    private enum Arg {
        USERNAME,
        PASSWORD, // six digits
        DATE_OF_BIRTH // ddMMyyyy
    }

    private final ArgumentManager<Arg> args = new ArgumentManager<>(Arg.values());

    @Before
    public void before() {
        args.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    private AgentIntegrationTest.Builder builder() {
        return new AgentIntegrationTest.Builder(IngConstants.MARKET, IngConstants.PROVIDER_NAME)
                .setFinancialInstitutionId("es-ing")
                .addCredentialField(Field.Key.USERNAME, args.get(Arg.USERNAME))
                .addCredentialField(IngConstants.DATE_OF_BIRTH, args.get(Arg.DATE_OF_BIRTH))
                .addCredentialField(Field.Key.PASSWORD, args.get(Arg.PASSWORD))
                .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder().build().testRefresh();
    }
}
