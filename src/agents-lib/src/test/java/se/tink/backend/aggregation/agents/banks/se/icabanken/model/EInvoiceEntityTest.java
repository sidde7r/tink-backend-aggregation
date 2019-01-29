package se.tink.backend.aggregation.agents.banks.se.icabanken.model;

import java.util.Date;
import org.junit.Assert;
import org.junit.Test;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.Transfer;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class EInvoiceEntityTest {
    private static final String UUID = "2016-04-26-16.20.03.177833";
    private static final String NAME = "TINK AB";
    private static final String DATE = "2016-05-26";
    private static final Double AMOUNT = 1.00;

    private static final Catalog CATALOG = Catalog.getCatalog("en_US");

    @Test
    public void testToTinkTransfer() {
        String accountNumber = "687-5496";
        EInvoiceEntity eInvoice = createEInvoiceEntity("PaymentBg", accountNumber);
        Transfer transfer = eInvoice.toTinkTransfer(CATALOG);

        Assert.assertEquals(transfer.getDestination(),
                AccountIdentifier.create(AccountIdentifier.Type.SE_BG, accountNumber));
        Assert.assertEquals(transfer.getAmount(), Amount.inSEK(AMOUNT));
        Assert.assertEquals(transfer.getType(), TransferType.EINVOICE);
        Assert.assertEquals(transfer.getDueDate(), stringToDate(DATE));
        Assert.assertEquals(transfer.getSourceMessage(), NAME);
        Assert.assertEquals(transfer.getDestinationMessage(), "N/A");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsException_WhenRecipientType_NotEqualTo_BgOrPg() {
        EInvoiceEntity eInvoice = createEInvoiceEntity("Transfer", "687-5496");

        eInvoice.toTinkTransfer(CATALOG);
    }

    @Test(expected = IllegalStateException.class)
    public void throwsException_WhenAccountNumber_IsNull() {
        EInvoiceEntity eInvoice = createEInvoiceEntity("PaymentBg", null);

        eInvoice.toTinkTransfer(CATALOG);
    }

    @Test(expected = IllegalStateException.class)
    public void throwsException_WhenAccountNumber_IsEmpty() {
        EInvoiceEntity eInvoice = createEInvoiceEntity("PaymentBg", "");
        eInvoice.toTinkTransfer(CATALOG);
    }

    private EInvoiceEntity createEInvoiceEntity(String type, String accountNumber) {
        EInvoiceEntity eInvoice = new EInvoiceEntity();

        eInvoice.setUuid(UUID);
        eInvoice.setName(NAME);
        eInvoice.setType(type);
        eInvoice.setAccountNumber(accountNumber);
        eInvoice.setDate(DATE);
        eInvoice.setAmount(AMOUNT);

        return eInvoice;
    }

    private Date stringToDate(String date) {
        return DateUtils.flattenTime(DateUtils.parseDate(date));
    }

    @Test
    public void testDeserializingSerialized() {
        // Given
        EInvoiceEntity entity = createEInvoiceEntity("PaymentBg", "190-7278");

        // When
        String serialized = SerializationUtils.serializeToString(entity);
        EInvoiceEntity deserialized = SerializationUtils.deserializeFromString(serialized, EInvoiceEntity.class);

        // Then
        Assert.assertNotNull("Could not deserialize EInvoiceEntity.", deserialized);
        Assert.assertEquals("PaymentBg", deserialized.getType());
        Assert.assertEquals(TransferType.EINVOICE, deserialized.toTinkTransfer(CATALOG).getType());
    }
}
