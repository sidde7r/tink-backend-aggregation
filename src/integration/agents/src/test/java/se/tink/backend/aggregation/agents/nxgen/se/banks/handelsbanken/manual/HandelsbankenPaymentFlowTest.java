package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.manual;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.date.CountryDateHelper;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.Transfer;

public class HandelsbankenPaymentFlowTest {

    private enum Arg implements ArgumentManager.ArgumentManagerEnum {
        SSN,
        SRC_ACCOUNT,
        DEST_BG_ACCOUNT; // Sweden Redcross BG number (900-8004)

        @Override
        public boolean isOptional() {
            return false;
        }
    }

    private final ArgumentManager<HandelsbankenPaymentFlowTest.Arg> manager =
            new ArgumentManager<>(HandelsbankenPaymentFlowTest.Arg.values());

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
                .addCredentialField(
                        Field.Key.USERNAME, manager.get(HandelsbankenPaymentFlowTest.Arg.SSN))
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .doLogout(true)
                .build()
                .testBankTransfer(transfer);
    }

    private Transfer createTransfer() {
        Locale locale = new Locale("sv", "SE");
        TimeZone timezone = TimeZone.getTimeZone("CET");
        CountryDateHelper dateHelper = new CountryDateHelper(locale, timezone);

        Transfer transfer = new Transfer();
        transfer.setSource(
                AccountIdentifier.create(
                        AccountIdentifier.Type.SE,
                        manager.get(HandelsbankenPaymentFlowTest.Arg.SRC_ACCOUNT)));

        transfer.setDestination(
                AccountIdentifier.create(
                        AccountIdentifier.Type.SE_BG,
                        manager.get(HandelsbankenPaymentFlowTest.Arg.DEST_BG_ACCOUNT)));
        transfer.setAmount(Amount.inSEK(1d));
        transfer.setType(TransferType.PAYMENT);
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, 1);
        transfer.setDueDate(c.getTime());
        transfer.setDestinationMessage("0123456789");

        return transfer;
    }
}
