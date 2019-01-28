package se.tink.backend.aggregation.agents.banks.se.icabanken.model;

import org.joda.time.LocalDate;
import org.junit.Test;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.backend.core.transfer.Transfer;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class UpcomingTransactionEntityTest {

    @Test
    public void getHash_whenFirstFourAmountDecimalsEqual_hashesEquals() {
        UpcomingTransactionEntity upcomingTransaction1 = new UpcomingTransactionEntity();
        upcomingTransaction1.setAmount(1.00001);
        upcomingTransaction1.setDueDate(new LocalDate().toString());
        upcomingTransaction1.setDestinationAccountNumber("730-8596");
        upcomingTransaction1.setType("Payment");
        upcomingTransaction1.setMemo("Gåva");
        upcomingTransaction1.setTransferMessage("BG Test payment");
        upcomingTransaction1.setSourceAccountNumber("92714943462");

        UpcomingTransactionEntity upcomingTransaction2 = new UpcomingTransactionEntity();
        upcomingTransaction2.setAmount(1.0);
        upcomingTransaction2.setDueDate(new LocalDate().toString());
        upcomingTransaction2.setDestinationAccountNumber("730-8596");
        upcomingTransaction2.setType("Payment");
        upcomingTransaction2.setMemo("Gåva");
        upcomingTransaction2.setTransferMessage("BG Test payment");
        upcomingTransaction2.setSourceAccountNumber("92714943462");

        assertEquals(upcomingTransaction1.getHash(false), upcomingTransaction2.getHash(false));
    }

    @Test
    public void getHash_whenFirstFourAmountDecimalsNotEqual_hashesNotEquals() {
        UpcomingTransactionEntity upcomingTransaction1 = new UpcomingTransactionEntity();
        upcomingTransaction1.setAmount(1.0001);
        upcomingTransaction1.setDueDate(new LocalDate().toString());
        upcomingTransaction1.setDestinationAccountNumber("730-8596");
        upcomingTransaction1.setType("Payment");
        upcomingTransaction1.setMemo("Gåva");
        upcomingTransaction1.setTransferMessage("BG Test payment");
        upcomingTransaction1.setSourceAccountNumber("92714943462");

        UpcomingTransactionEntity upcomingTransaction2 = new UpcomingTransactionEntity();
        upcomingTransaction2.setAmount(1.0);
        upcomingTransaction2.setDueDate(new LocalDate().toString());
        upcomingTransaction2.setDestinationAccountNumber("730-8596");
        upcomingTransaction2.setType("Payment");
        upcomingTransaction2.setMemo("Gåva");
        upcomingTransaction2.setTransferMessage("BG Test payment");
        upcomingTransaction2.setSourceAccountNumber("92714943462");

        assertNotEquals(upcomingTransaction1.getHash(false), upcomingTransaction2.getHash(false));
    }

    @Test
    public void getHash_whenDueDateDiffer_hashesNotEquals() {
        UpcomingTransactionEntity upcomingTransaction1 = new UpcomingTransactionEntity();
        upcomingTransaction1.setAmount(1.0);
        upcomingTransaction1.setDueDate(new LocalDate().minusDays(1).toString());
        upcomingTransaction1.setDestinationAccountNumber("730-8596");
        upcomingTransaction1.setType("Payment");
        upcomingTransaction1.setMemo("Gåva");
        upcomingTransaction1.setTransferMessage("BG Test payment");
        upcomingTransaction1.setSourceAccountNumber("92714943462");

        UpcomingTransactionEntity upcomingTransaction2 = new UpcomingTransactionEntity();
        upcomingTransaction2.setAmount(1.0);
        upcomingTransaction2.setDueDate(new LocalDate().toString());
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
        upcomingTransaction1.setDueDate(new LocalDate().toString());
        upcomingTransaction1.setDestinationAccountNumber("730-8593");
        upcomingTransaction1.setType("Payment");
        upcomingTransaction1.setMemo("Gåva");
        upcomingTransaction1.setTransferMessage("BG Test payment");
        upcomingTransaction1.setSourceAccountNumber("92714943462");

        UpcomingTransactionEntity upcomingTransaction2 = new UpcomingTransactionEntity();
        upcomingTransaction2.setAmount(1.0);
        upcomingTransaction2.setDueDate(new LocalDate().toString());
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
        upcomingTransaction1.setDueDate(new LocalDate().toString());
        upcomingTransaction1.setDestinationAccountNumber("730-8596");
        upcomingTransaction1.setType("Transfer");
        upcomingTransaction1.setMemo("Gåva");
        upcomingTransaction1.setTransferMessage("BG Test payment");
        upcomingTransaction1.setSourceAccountNumber("92714943462");

        UpcomingTransactionEntity upcomingTransaction2 = new UpcomingTransactionEntity();
        upcomingTransaction2.setAmount(1.0);
        upcomingTransaction2.setDueDate(new LocalDate().toString());
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
        upcomingTransaction1.setDueDate(new LocalDate().toString());
        upcomingTransaction1.setDestinationAccountNumber("730-8596");
        upcomingTransaction1.setType("Payment");
        upcomingTransaction1.setMemo("Gåvan");
        upcomingTransaction1.setTransferMessage("BG Test payment");
        upcomingTransaction1.setSourceAccountNumber("92714943462");

        UpcomingTransactionEntity upcomingTransaction2 = new UpcomingTransactionEntity();
        upcomingTransaction2.setAmount(1.0);
        upcomingTransaction2.setDueDate(new LocalDate().toString());
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
        upcomingTransaction1.setDueDate(new LocalDate().toString());
        upcomingTransaction1.setDestinationAccountNumber("730-8596");
        upcomingTransaction1.setType("Payment");
        upcomingTransaction1.setMemo("Gåva");
        upcomingTransaction1.setTransferMessage("PG Test payment");
        upcomingTransaction1.setSourceAccountNumber("92714943462");

        UpcomingTransactionEntity upcomingTransaction2 = new UpcomingTransactionEntity();
        upcomingTransaction2.setAmount(1.0);
        upcomingTransaction2.setDueDate(new LocalDate().toString());
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
        upcomingTransaction1.setDueDate(new LocalDate().toString());
        upcomingTransaction1.setDestinationAccountNumber("730-8596");
        upcomingTransaction1.setType("Payment");
        upcomingTransaction1.setMemo("Gåva");
        upcomingTransaction1.setTransferMessage("BG Test payment");
        upcomingTransaction1.setSourceAccountNumber("92714943462");

        UpcomingTransactionEntity upcomingTransaction2 = new UpcomingTransactionEntity();
        upcomingTransaction2.setAmount(1.0);
        upcomingTransaction2.setDueDate(new LocalDate().toString());
        upcomingTransaction2.setDestinationAccountNumber("730-8596");
        upcomingTransaction2.setType("Payment");
        upcomingTransaction2.setMemo("Gåva");
        upcomingTransaction2.setTransferMessage("BG Test payment");
        upcomingTransaction2.setSourceAccountNumber("92714943461");

        assertNotEquals(upcomingTransaction1.getHash(false), upcomingTransaction2.getHash(false));
    }

    @Test
    public void getHash_compareWithTransferHash() {
        UpcomingTransactionEntity upcomingTransaction1 = new UpcomingTransactionEntity();
        upcomingTransaction1.setAmount(1.0);
        upcomingTransaction1.setDueDate(new LocalDate().toString());
        upcomingTransaction1.setDestinationAccountNumber("730-8596");
        upcomingTransaction1.setType("Payment");
        upcomingTransaction1.setMemo("BG Test payment");
        upcomingTransaction1.setTransferMessage("38173926046183528");
        upcomingTransaction1.setSourceAccountNumber("92714943462");

        Transfer t = new Transfer();

        AccountIdentifier identifier = new BankGiroIdentifier("730-8596");

        t.setAmount(Amount.inSEK(1.0));
        t.setDueDate(new LocalDate().toDate());
        t.setSource(new SwedishIdentifier("92714943462"));
        t.setDestination(identifier);
        t.setDestinationMessage("38173926046183528");
        t.setSourceMessage("BG Test payment");
        t.setType(TransferType.PAYMENT);

        assertEquals(t.getHash(), upcomingTransaction1.getHash(false));
    }

}
