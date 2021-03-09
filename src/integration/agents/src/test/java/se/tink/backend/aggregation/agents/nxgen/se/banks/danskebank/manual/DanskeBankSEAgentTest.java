package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.manual;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.ArgumentManagerEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.SsnArgumentEnum;
import se.tink.libraries.credentials.service.RefreshableItem;

public final class DanskeBankSEAgentTest {
    private enum Arg implements ArgumentManagerEnum {
        SERVICE_CODE;

        @Override
        public boolean isOptional() {
            return false;
        }
    }

    private static final ArgumentManager<Arg> manager = new ArgumentManager<>(Arg.values());
    private static final ArgumentManager<SsnArgumentEnum> ssnManager =
            new ArgumentManager<>(SsnArgumentEnum.values());

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        manager.before();
        ssnManager.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testRefreshWithBankId() throws Exception {
        builder =
                new AgentIntegrationTest.Builder("se", "se-danskebank-bankid")
                        .addCredentialField(Field.Key.USERNAME, ssnManager.get(SsnArgumentEnum.SSN))
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);
        builder.build().testRefresh();
    }

    @Test
    public void testRefreshWithServiceCode() throws Exception {
        builder =
                new AgentIntegrationTest.Builder("se", "se-danskebank-password")
                        .addCredentialField(Field.Key.USERNAME, ssnManager.get(SsnArgumentEnum.SSN))
                        .addCredentialField(Field.Key.PASSWORD, manager.get(Arg.SERVICE_CODE))
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);
        builder.build().testRefresh();
    }
}
