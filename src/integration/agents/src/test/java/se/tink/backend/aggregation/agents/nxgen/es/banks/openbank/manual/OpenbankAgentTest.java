package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.manual;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.ArgumentManagerEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernamePasswordArgumentEnum;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.OpenbankConstants;
import se.tink.libraries.credentials.service.RefreshableItem;

public class OpenbankAgentTest {

    private final ArgumentManager<UsernamePasswordArgumentEnum> usernamePasswordhelper =
            new ArgumentManager<>(UsernamePasswordArgumentEnum.values());
    private final ArgumentManager<Arg> usernameTypeHelper = new ArgumentManager<>(Arg.values());

    private AgentIntegrationTest.Builder builder;

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void setup() {
        usernamePasswordhelper.before();
        usernameTypeHelper.before();

        builder =
                new AgentIntegrationTest.Builder("es", "es-openbank-password")
                        .addCredentialField(
                                Field.Key.USERNAME,
                                usernamePasswordhelper.get(UsernamePasswordArgumentEnum.USERNAME))
                        .addCredentialField(
                                OpenbankConstants.USERNAME_TYPE,
                                usernameTypeHelper.get(Arg.USERNAME_TYPE))
                        .addCredentialField(
                                Field.Key.PASSWORD,
                                usernamePasswordhelper.get(UsernamePasswordArgumentEnum.PASSWORD))
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }

    private enum Arg implements ArgumentManagerEnum {
        USERNAME_TYPE;

        @Override
        public boolean isOptional() {
            return false;
        }
    }
}
