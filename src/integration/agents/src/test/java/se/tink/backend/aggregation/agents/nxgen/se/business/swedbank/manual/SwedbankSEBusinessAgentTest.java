package se.tink.backend.aggregation.agents.nxgen.se.business.swedbank.manual;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.BusinessIdArgumentEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.SsnArgumentEnum;
import se.tink.libraries.credentials.service.RefreshableItem;

public class SwedbankSEBusinessAgentTest {
    private final ArgumentManager<SsnArgumentEnum> ssnManager =
            new ArgumentManager<>(SsnArgumentEnum.values());
    private final ArgumentManager<BusinessIdArgumentEnum> psuIdManager =
            new ArgumentManager<>(BusinessIdArgumentEnum.values());

    @Before
    public void setUp() throws Exception {
        ssnManager.before();
        psuIdManager.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("se", "swedbank-business-bankid")
                .addCredentialField(Field.Key.USERNAME, ssnManager.get(SsnArgumentEnum.SSN))
                .addCredentialField(
                        Field.Key.CORPORATE_ID, psuIdManager.get(BusinessIdArgumentEnum.CPI))
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                .build()
                .testRefresh();
    }
}
