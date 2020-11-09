package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.einvoice.entities.PaymentEntity;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;
import se.tink.libraries.transfer.rpc.Transfer;

public class PaymentEntityTest {

    private static final String SOURCE_ACCOUNT = "54910000003";
    private static final String DESTINATION_ACCOUNT = "11001199";
    private static final String MESSAGE = "Message";

    @Test
    public void transferShouldBeSameAsExistingUnsignedPayment() {
        Calendar c = Calendar.getInstance();
        Transfer transfer = createTransfer();
        PaymentEntity paymentEntity = createPaymentEntity(c.getTime());
        assertTrue(paymentEntity.isEqualToTransfer(transfer, c.getTime()));
    }

    @Test
    public void transferShouldNotBeSameAsExistingUnsignedPayment() {
        Calendar c = Calendar.getInstance();
        Transfer transfer = createTransfer();
        PaymentEntity paymentEntity = createPaymentEntity(c.getTime());
        assertFalse(paymentEntity.isEqualToTransfer(transfer, null));
    }

    private Transfer createTransfer() {
        Transfer transfer = new Transfer();
        transfer.setSource(AccountIdentifier.create(Type.SE, SOURCE_ACCOUNT));
        transfer.setDestination(AccountIdentifier.create(Type.SE_BG, DESTINATION_ACCOUNT));
        transfer.setAmount(ExactCurrencyAmount.inSEK(1));
        transfer.setType(TransferType.PAYMENT);
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue(MESSAGE);
        transfer.setRemittanceInformation(remittanceInformation);
        return transfer;
    }

    private PaymentEntity createPaymentEntity(Date dueDate) {
        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setAmount(1d);
        paymentEntity.setRecipientAccountNumber(DESTINATION_ACCOUNT);
        paymentEntity.setFrom(SOURCE_ACCOUNT);
        paymentEntity.setDue(dueDate);
        paymentEntity.setMessage(MESSAGE);
        return paymentEntity;
    }
}
