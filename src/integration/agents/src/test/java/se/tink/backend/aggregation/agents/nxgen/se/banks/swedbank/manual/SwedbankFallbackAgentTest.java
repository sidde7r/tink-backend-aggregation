package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.manual;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.SsnArgumentEnum;
import se.tink.libraries.credentials.service.RefreshableItem;

public class SwedbankFallbackAgentTest {
    private AgentIntegrationTest.Builder builder =
            new AgentIntegrationTest.Builder("se", "se-swedbank-fallback")
                    .setFinancialInstitutionId("swedbank")
                    .setAppId("tink")
                    .setClusterId("oxford-preprod")
                    .loadCredentialsBefore(false)
                    .saveCredentialsAfter(false)
                    .expectLoggedIn(false);

    private final ArgumentManager<SsnArgumentEnum> ssnManager =
            new ArgumentManager<>(SsnArgumentEnum.values());

    @Before
    public void setUp() throws Exception {
        ssnManager.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testRefresh() throws Exception {
        builder.addCredentialField(Field.Key.USERNAME, ssnManager.get(SsnArgumentEnum.SSN))
                .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                .build()
                .testRefresh();
    }
}
