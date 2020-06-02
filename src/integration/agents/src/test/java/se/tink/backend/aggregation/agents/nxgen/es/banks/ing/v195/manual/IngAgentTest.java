package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.manual;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.ArgumentManagerEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernamePasswordArgumentEnum;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngConstants;
import se.tink.libraries.credentials.service.RefreshableItem;

public class IngAgentTest {
    private enum Arg implements ArgumentManagerEnum {
        DATE_OF_BIRTH; // ddMMyyyy

        @Override
        public boolean isOptional() {
            return false;
        }
    }

    private final ArgumentManager<Arg> dateOfBirthManager = new ArgumentManager<>(Arg.values());
    private final ArgumentManager<UsernamePasswordArgumentEnum> usernamePasswordManager =
            new ArgumentManager<>(UsernamePasswordArgumentEnum.values());

    @Before
    public void before() {
        dateOfBirthManager.before();
        usernamePasswordManager.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    private AgentIntegrationTest.Builder builder() {
        return new AgentIntegrationTest.Builder(IngConstants.MARKET, IngConstants.PROVIDER_NAME)
                .setFinancialInstitutionId("es-ing")
                .addCredentialField(
                        Field.Key.USERNAME,
                        usernamePasswordManager.get(UsernamePasswordArgumentEnum.USERNAME))
                .addCredentialField(
                        IngConstants.DATE_OF_BIRTH, dateOfBirthManager.get(Arg.DATE_OF_BIRTH))
                .addCredentialField(
                        Field.Key.PASSWORD,
                        usernamePasswordManager.get(UsernamePasswordArgumentEnum.PASSWORD))
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
