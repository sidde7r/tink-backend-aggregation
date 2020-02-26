package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.SsnArgumentEnum;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.Transfer;

import java.util.Calendar;

public class HandelsbankenSEAgentTest {
    private AgentIntegrationTest.Builder builder =
            new AgentIntegrationTest.Builder("se", "handelsbanken-bankid")
                    .loadCredentialsBefore(false)
                    .saveCredentialsAfter(false)
                    .doLogout(true);
    private final ArgumentManager<SsnArgumentEnum> manager =
            new ArgumentManager<>(SsnArgumentEnum.values());

    @Before
    public void setUp() throws Exception {
        manager.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void test() throws Exception {
        new AgentIntegrationTest.Builder("se", "handelsbanken-bankid")
                .addCredentialField(Field.Key.USERNAME, manager.get(SsnArgumentEnum.SSN))
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                .build()
                .testRefresh();
    }

    @Test
    public void testPayment() throws Exception {
        Transfer transfer = new Transfer();
        transfer.setSource(
                AccountIdentifier.create(AccountIdentifier.Type.SE,"" ));
        transfer.setDestination(AccountIdentifier.create(AccountIdentifier.Type.SE_BG, "900-8004"));
        transfer.setAmount(Amount.inSEK(1d));
        transfer.setType(TransferType.PAYMENT);
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, 1);
        transfer.setDueDate(c.getTime());
        transfer.setDestinationMessage("32456789");

        new AgentIntegrationTest.Builder("se", "handelsbanken-bankid")
                .addCredentialField(Field.Key.USERNAME, manager.get(SsnArgumentEnum.SSN))
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .doLogout(true)
                .build()
                .testBankTransfer(transfer);
    }
}
