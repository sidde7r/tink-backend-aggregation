package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.manual;

import java.util.Calendar;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;
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
        Transfer transfer = new Transfer();
        transfer.setSource(
                AccountIdentifier.create(
                        AccountIdentifierType.SE,
                        manager.get(HandelsbankenPaymentFlowTest.Arg.SRC_ACCOUNT)));

        transfer.setDestination(
                AccountIdentifier.create(
                        AccountIdentifierType.SE_BG,
                        manager.get(HandelsbankenPaymentFlowTest.Arg.DEST_BG_ACCOUNT)));
        transfer.setAmount(ExactCurrencyAmount.inSEK(1d));
        transfer.setType(TransferType.PAYMENT);
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, 1);
        transfer.setDueDate(c.getTime());
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("0123456789");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        transfer.setRemittanceInformation(remittanceInformation);

        return transfer;
    }
}
