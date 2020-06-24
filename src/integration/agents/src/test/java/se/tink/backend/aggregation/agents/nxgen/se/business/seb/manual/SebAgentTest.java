package se.tink.backend.aggregation.agents.nxgen.se.business.seb.manual;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.BusinessIdArgumentEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.SsnArgumentEnum;
import se.tink.libraries.credentials.service.RefreshableItem;

public class SebAgentTest {

    private final ArgumentManager<SsnArgumentEnum> manager =
            new ArgumentManager<>(SsnArgumentEnum.values());
    private final ArgumentManager<BusinessIdArgumentEnum> psuIdManager =
            new ArgumentManager<>(BusinessIdArgumentEnum.values());

    private AgentIntegrationTest.Builder sebBankId;
    private AgentIntegrationTest.Builder sebToken;

    @Before
    public void before() {
        manager.before();
        psuIdManager.before();

        sebBankId =
                new AgentIntegrationTest.Builder("se", "seb-business-bankid")
                        .addCredentialField(Field.Key.USERNAME, manager.get(SsnArgumentEnum.SSN))
                        .addCredentialField(
                                Field.Key.CORPORATE_ID,
                                psuIdManager.get(BusinessIdArgumentEnum.CPI))
                        .addRefreshableItems(RefreshableItem.CHECKING_ACCOUNTS)
                        .addRefreshableItems(RefreshableItem.CHECKING_TRANSACTIONS)
                        .addRefreshableItems(RefreshableItem.SAVING_ACCOUNTS)
                        .addRefreshableItems(RefreshableItem.SAVING_TRANSACTIONS)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(true);
        sebToken =
                new AgentIntegrationTest.Builder("se", "seb-business-token")
                        .addCredentialField(Field.Key.USERNAME, manager.get(SsnArgumentEnum.SSN))
                        .addCredentialField(
                                Field.Key.CORPORATE_ID,
                                psuIdManager.get(BusinessIdArgumentEnum.CPI))
                        .addRefreshableItems(RefreshableItem.CHECKING_ACCOUNTS)
                        .addRefreshableItems(RefreshableItem.CHECKING_TRANSACTIONS)
                        .addRefreshableItems(RefreshableItem.SAVING_ACCOUNTS)
                        .addRefreshableItems(RefreshableItem.SAVING_TRANSACTIONS)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(true);
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testBankIdLoginAndRefresh() throws Exception {
        sebBankId.build().testRefresh();
    }

    @Test
    public void testTokenLoginAndRefresh() throws Exception {
        sebToken.build().testRefresh();
    }
}
