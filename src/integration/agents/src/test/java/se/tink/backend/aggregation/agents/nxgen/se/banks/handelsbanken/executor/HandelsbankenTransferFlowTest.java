package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor;

import java.util.Locale;
import java.util.TimeZone;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.date.CountryDateHelper;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.Transfer;

public class HandelsbankenTransferFlowTest {

    private enum Arg {
        USERNAME,
        SRC_ACCOUNT,
        DEST_ACCOUNT
    }

    private final ArgumentManager<Arg> manager = new ArgumentManager<>(Arg.values());

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

        Transfer transfer = createTransfer();

        new AgentIntegrationTest.Builder("se", "handelsbanken-bankid")
                .addCredentialField(Field.Key.USERNAME, manager.get(Arg.USERNAME))
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                .build()
                .testBankTransfer(transfer);
    }

    private Transfer createTransfer() {
        Locale locale = new Locale("sv", "SE");
        TimeZone timezone = TimeZone.getTimeZone("CET");
        CountryDateHelper dateHelper = new CountryDateHelper(locale, timezone);

        Transfer transfer = new Transfer();

        transfer.setAmount(Amount.inSEK(1.0));
        transfer.setDestinationMessage("Tink dest");
        transfer.setSourceMessage("Tink source");
        transfer.setType(TransferType.BANK_TRANSFER);
        transfer.setDueDate(dateHelper.addDays(dateHelper.getToday(), 7));

        transfer.setSource(new SwedishIdentifier(manager.get(Arg.SRC_ACCOUNT)));
        transfer.setDestination(new SwedishIdentifier(manager.get(Arg.DEST_ACCOUNT)));

        return transfer;
    }
}
