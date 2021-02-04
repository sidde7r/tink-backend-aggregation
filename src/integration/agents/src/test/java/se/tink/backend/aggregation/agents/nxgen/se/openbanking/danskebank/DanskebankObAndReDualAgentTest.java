package se.tink.backend.aggregation.agents.nxgen.se.openbanking.danskebank;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.SsnArgumentEnum;
import se.tink.backend.aggregation.agents.framework.DualAgentIntegrationTest;
import se.tink.libraries.credentials.service.RefreshableItem;

public class DanskebankObAndReDualAgentTest {
    private final ArgumentManager<SsnArgumentEnum> ssnManager =
            new ArgumentManager<>(SsnArgumentEnum.values());

    @Before
    public void setup() {
        ssnManager.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void dualTest() throws Exception {
        DualAgentIntegrationTest.of(
                        new AgentIntegrationTest.Builder("se", "se-danskebank-ob")
                                .setAppId("tink")
                                .setFinancialInstitutionId("danskebank")
                                .expectLoggedIn(false)
                                .loadCredentialsBefore(true)
                                .saveCredentialsAfter(true)
                                .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                                .build(),
                        new AgentIntegrationTest.Builder("se", "se-danskebank-bankid")
                                .addCredentialField(
                                        Field.Key.USERNAME, ssnManager.get(SsnArgumentEnum.SSN))
                                .expectLoggedIn(false)
                                .loadCredentialsBefore(false)
                                .saveCredentialsAfter(false)
                                .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                                .build())
                .testAndCompare();
    }
}
