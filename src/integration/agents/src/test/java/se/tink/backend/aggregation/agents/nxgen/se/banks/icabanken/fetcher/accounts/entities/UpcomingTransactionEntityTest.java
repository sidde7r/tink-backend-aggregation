package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.entities;

import static org.junit.Assert.assertNotEquals;

import java.util.Date;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;
import se.tink.libraries.transfer.rpc.Transfer;

public class UpcomingTransactionEntityTest {

    @Test
    public void getHash_whenDueDateDiffer_hashesNotEquals() {
        UpcomingTransactionEntity upcomingTransaction1 = new UpcomingTransactionEntity();
        upcomingTransaction1.setAmount(1.0);
        upcomingTransaction1.setDueDate(DateUtils.addDays(new Date(), -1));
        upcomingTransaction1.setDestinationAccountNumber("730-8596");
        upcomingTransaction1.setType("Payment");
        upcomingTransaction1.setMemo("Gåva");
        upcomingTransaction1.setTransferMessage("BG Test payment");
        upcomingTransaction1.setSourceAccountNumber("92714943462");

        UpcomingTransactionEntity upcomingTransaction2 = new UpcomingTransactionEntity();
        upcomingTransaction2.setAmount(1.0);
        upcomingTransaction2.setDueDate(new Date());
        upcomingTransaction2.setDestinationAccountNumber("730-8596");
        upcomingTransaction2.setType("Payment");
        upcomingTransaction2.setMemo("Gåva");
        upcomingTransaction2.setTransferMessage("BG Test payment");
        upcomingTransaction2.setSourceAccountNumber("92714943462");

        assertNotEquals(upcomingTransaction1.getHash(false), upcomingTransaction2.getHash(false));
    }

    @Test
    public void getHash_DestinationAccountDiffer_hashesNotEquals() {
        UpcomingTransactionEntity upcomingTransaction1 = new UpcomingTransactionEntity();
        upcomingTransaction1.setAmount(1.0);
        upcomingTransaction1.setDueDate(new Date());
        upcomingTransaction1.setDestinationAccountNumber("730-8593");
        upcomingTransaction1.setType("Payment");
        upcomingTransaction1.setMemo("Gåva");
        upcomingTransaction1.setTransferMessage("BG Test payment");
        upcomingTransaction1.setSourceAccountNumber("92714943462");

        UpcomingTransactionEntity upcomingTransaction2 = new UpcomingTransactionEntity();
        upcomingTransaction2.setAmount(1.0);
        upcomingTransaction2.setDueDate(new Date());
        upcomingTransaction2.setDestinationAccountNumber("730-8596");
        upcomingTransaction2.setType("Payment");
        upcomingTransaction2.setMemo("Gåva");
        upcomingTransaction2.setTransferMessage("BG Test payment");
        upcomingTransaction2.setSourceAccountNumber("92714943462");

        assertNotEquals(upcomingTransaction1.getHash(false), upcomingTransaction2.getHash(false));
    }

    @Test
    public void getHash_TypeDiffer_hashesNotEquals() {
        UpcomingTransactionEntity upcomingTransaction1 = new UpcomingTransactionEntity();
        upcomingTransaction1.setAmount(1.0);
        upcomingTransaction1.setDueDate(new Date());
        upcomingTransaction1.setDestinationAccountNumber("730-8596");
        upcomingTransaction1.setType("Transfer");
        upcomingTransaction1.setMemo("Gåva");
        upcomingTransaction1.setTransferMessage("BG Test payment");
        upcomingTransaction1.setSourceAccountNumber("92714943462");

        UpcomingTransactionEntity upcomingTransaction2 = new UpcomingTransactionEntity();
        upcomingTransaction2.setAmount(1.0);
        upcomingTransaction2.setDueDate(new Date());
        upcomingTransaction2.setDestinationAccountNumber("730-8596");
        upcomingTransaction2.setType("Payment");
        upcomingTransaction2.setMemo("Gåva");
        upcomingTransaction2.setTransferMessage("BG Test payment");
        upcomingTransaction2.setSourceAccountNumber("92714943462");

        assertNotEquals(upcomingTransaction1.getHash(false), upcomingTransaction2.getHash(false));
    }

    @Test
    public void getHash_MemoDiffer_hashesNotEquals() {
        UpcomingTransactionEntity upcomingTransaction1 = new UpcomingTransactionEntity();
        upcomingTransaction1.setAmount(1.0);
        upcomingTransaction1.setDueDate(new Date());
        upcomingTransaction1.setDestinationAccountNumber("730-8596");
        upcomingTransaction1.setType("Payment");
        upcomingTransaction1.setMemo("Gåvan");
        upcomingTransaction1.setTransferMessage("BG Test payment");
        upcomingTransaction1.setSourceAccountNumber("92714943462");

        UpcomingTransactionEntity upcomingTransaction2 = new UpcomingTransactionEntity();
        upcomingTransaction2.setAmount(1.0);
        upcomingTransaction2.setDueDate(new Date());
        upcomingTransaction2.setDestinationAccountNumber("730-8596");
        upcomingTransaction2.setType("Payment");
        upcomingTransaction2.setMemo("Gåva");
        upcomingTransaction2.setTransferMessage("BG Test payment");
        upcomingTransaction2.setSourceAccountNumber("92714943462");

        assertNotEquals(upcomingTransaction1.getHash(false), upcomingTransaction2.getHash(false));
    }

    @Test
    public void getHash_TransferMessageDiffer_hashesNotEquals() {
        UpcomingTransactionEntity upcomingTransaction1 = new UpcomingTransactionEntity();
        upcomingTransaction1.setAmount(1.0);
        upcomingTransaction1.setDueDate(new Date());
        upcomingTransaction1.setDestinationAccountNumber("730-8596");
        upcomingTransaction1.setType("Payment");
        upcomingTransaction1.setMemo("Gåva");
        upcomingTransaction1.setTransferMessage("PG Test payment");
        upcomingTransaction1.setSourceAccountNumber("92714943462");

        UpcomingTransactionEntity upcomingTransaction2 = new UpcomingTransactionEntity();
        upcomingTransaction2.setAmount(1.0);
        upcomingTransaction2.setDueDate(new Date());
        upcomingTransaction2.setDestinationAccountNumber("730-8596");
        upcomingTransaction2.setType("Payment");
        upcomingTransaction2.setMemo("Gåva");
        upcomingTransaction2.setTransferMessage("BG Test payment");
        upcomingTransaction2.setSourceAccountNumber("92714943462");

        assertNotEquals(upcomingTransaction1.getHash(false), upcomingTransaction2.getHash(false));
    }

    @Test
    public void getHash_SourceAccountDiffer_hashesNotEquals() {
        UpcomingTransactionEntity upcomingTransaction1 = new UpcomingTransactionEntity();
        upcomingTransaction1.setAmount(1.0);
        upcomingTransaction1.setDueDate(new Date());
        upcomingTransaction1.setDestinationAccountNumber("730-8596");
        upcomingTransaction1.setType("Payment");
        upcomingTransaction1.setMemo("Gåva");
        upcomingTransaction1.setTransferMessage("BG Test payment");
        upcomingTransaction1.setSourceAccountNumber("92714943462");

        UpcomingTransactionEntity upcomingTransaction2 = new UpcomingTransactionEntity();
        upcomingTransaction2.setAmount(1.0);
        upcomingTransaction2.setDueDate(new Date());
        upcomingTransaction2.setDestinationAccountNumber("730-8596");
        upcomingTransaction2.setType("Payment");
        upcomingTransaction2.setMemo("Gåva");
        upcomingTransaction2.setTransferMessage("BG Test payment");
        upcomingTransaction2.setSourceAccountNumber("92714943461");

        assertNotEquals(upcomingTransaction1.getHash(false), upcomingTransaction2.getHash(false));
    }

    @Test
    public void getHash_compareWithTransferHashForInvalidDestination() {
        UpcomingTransactionEntity upcomingTransaction1 = new UpcomingTransactionEntity();
        upcomingTransaction1.setAmount(1.0);
        upcomingTransaction1.setDueDate(new Date());
        upcomingTransaction1.setDestinationAccountNumber("asdfasdfdsfd");
        upcomingTransaction1.setType("Payment");
        upcomingTransaction1.setMemo("BG Test payment");
        upcomingTransaction1.setTransferMessage("38173926046183528");
        upcomingTransaction1.setSourceAccountNumber("92714943462");

        Transfer t = new Transfer();

        AccountIdentifier identifier = new BankGiroIdentifier("730-8596");

        t.setAmount(ExactCurrencyAmount.inSEK(1.0));
        t.setDueDate(new Date());
        t.setSource(new SwedishIdentifier("92714943462"));
        t.setDestination(identifier);
        t.setRemittanceInformation(
                getRemittanceInformation(RemittanceInformationType.OCR, "38173926046183528"));
        t.setSourceMessage("BG Test payment");
        t.setType(TransferType.PAYMENT);

        assertNotEquals(t.getHash(), upcomingTransaction1.getHash(false));
    }

    @Test
    public void getHash_compareWithTransferHashForNullDestination() {
        UpcomingTransactionEntity upcomingTransaction1 = new UpcomingTransactionEntity();
        upcomingTransaction1.setAmount(1.0);
        upcomingTransaction1.setDueDate(new Date());
        upcomingTransaction1.setDestinationAccountNumber(null);
        upcomingTransaction1.setType("Payment");
        upcomingTransaction1.setMemo("BG Test payment");
        upcomingTransaction1.setTransferMessage("38173926046183528");
        upcomingTransaction1.setSourceAccountNumber("92714943462");

        Transfer t = new Transfer();

        AccountIdentifier identifier = new BankGiroIdentifier("730-8596");

        t.setAmount(ExactCurrencyAmount.inSEK(1.0));
        t.setDueDate(new Date());
        t.setSource(new SwedishIdentifier("92714943462"));
        t.setDestination(identifier);
        t.setRemittanceInformation(
                getRemittanceInformation(RemittanceInformationType.OCR, "38173926046183528"));
        t.setSourceMessage("BG Test payment");
        t.setType(TransferType.PAYMENT);

        assertNotEquals(t.getHash(), upcomingTransaction1.getHash(false));
    }

    private RemittanceInformation getRemittanceInformation(
            RemittanceInformationType type, String value) {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(type);
        remittanceInformation.setValue(value);
        return remittanceInformation;
    }
}
