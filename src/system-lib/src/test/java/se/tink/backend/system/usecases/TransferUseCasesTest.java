package se.tink.backend.system.usecases;

import com.google.common.collect.ImmutableSet;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import se.tink.backend.common.repository.cassandra.TransferInMemoryRepository;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.backend.core.Amount;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.backend.core.enums.TransferType;
import se.tink.backend.core.transfer.Transfer;
import se.tink.libraries.date.DateUtils;
import static org.junit.Assert.assertTrue;

@RunWith(Enclosed.class)
public class TransferUseCasesTest {

    protected static String userIdWithoutTransfers = "9cf8f338fafd41e2af883d95825c6cd6";
    protected static String userIdWithoutEInvoices = "5cfb6797391e4746abbf992bc33c775a";
    protected static String userIdWithEInvoices = "e3cf02e38aec41dc9ab3d02351a2927d";

    protected static UUID id1 = UUID.fromString("D9D84125-6E16-4F93-A5A7-848E7BBFBF6E");
    protected static UUID id2 = UUID.fromString("F14C389B-0AC4-4FD8-AE1C-48CD9DF84BC8");
    protected static UUID id3 = UUID.fromString("41F63541-102C-4B87-8C0E-D55FB8FEF852");
    protected static UUID id4 = UUID.fromString("CF1600F4-01D1-4318-8EE7-F68F208EB8AE");
    protected static UUID id5 = UUID.fromString("F5F1E1AB-782F-4B19-AE29-B37E5E24FA28");

    protected static UUID credentialsId1 = UUID.fromString("BDA80EC2-5989-4068-BC71-EA4E3FF0AFCF");
    protected static String tinkCredentialsId1 = UUIDUtils.toTinkUUID(credentialsId1);

    protected static UUID credentialsId2 = UUID.fromString("E0C1F679-A3EB-437B-ABD3-34335AEFC8E9");

    private static Transfer createTransfer(UUID credentialsId, TransferType type, String destUri, String destMessage, Date dueDate, String userId) {
        return createTransfer(UUID.randomUUID(), credentialsId, type, destUri, destMessage, dueDate, userId);
    }

    private static Transfer createTransfer(UUID id, UUID credentialsId, TransferType type, String destUri, String destMessage, Date dueDate, String userId) {
        Transfer t = new Transfer();
        t.setId(id);
        t.setCredentialsId(credentialsId);
        t.setType(type);
        t.setAmount(Amount.inSEK(100d));
        t.setDestinationMessage(destMessage);
        t.setDueDate(dueDate);
        t.setSource(new SwedishIdentifier("6134123456789"));
        t.setDestination(AccountIdentifier.create(URI.create(destUri)));
        t.setSourceMessage("betalning");
        t.setUserId(UUIDUtils.fromTinkUUID(userId));
        return t;
    }

    private static Date dateFromNow(int numDays) {
        Date date = new Date();
        return DateUtils.addDays(date, numDays);
    }

    private static List<Transfer> oneIncomingEinvoices(String userId) {
        return Collections.singletonList(
                createTransfer(credentialsId1, TransferType.EINVOICE, "se-bg://12345671", "telia", dateFromNow(5),
                        userId));
    }

    private static List<Transfer> pairOfDuplicateIncomingEinvoices(String userId) {
        return Arrays.asList(
                createTransfer(credentialsId1, TransferType.EINVOICE, "se-bg://12345671", "telia", dateFromNow(5),
                        userId),
                createTransfer(credentialsId1, TransferType.EINVOICE, "se-bg://12345671", "telia", dateFromNow(5),
                        userId));
    }

    private static List<Transfer> twoPairsOfDuplicateIncomingEinvoices(String userId) {
        return Arrays.asList(
                createTransfer(credentialsId1, TransferType.EINVOICE, "se-bg://12345671", "telia", dateFromNow(5),
                        userId),
                createTransfer(credentialsId1, TransferType.EINVOICE, "se-bg://12345671", "telia", dateFromNow(5),
                        userId),
                createTransfer(credentialsId1, TransferType.EINVOICE, "se-bg://7654324", "amex faktura",
                        dateFromNow(21), userId),
                createTransfer(credentialsId1, TransferType.EINVOICE, "se-bg://7654324", "amex faktura",
                        dateFromNow(21), userId));
    }

    private static List<Transfer> twoIncomingEinvoices(String userId) {
        return Arrays.asList(
                createTransfer(credentialsId1, TransferType.EINVOICE, "se-bg://12345671", "telia", dateFromNow(5), userId),
                createTransfer(credentialsId1, TransferType.EINVOICE, "se-bg://7654324", "amex faktura", dateFromNow(21), userId));
    }

    private static List<Transfer> threeIncomingEinvoices(String userId) {
        return Arrays.asList(
                createTransfer(credentialsId1, TransferType.EINVOICE, "se-bg://12345671", "telia", dateFromNow(5), userId),
                createTransfer(credentialsId1, TransferType.EINVOICE, "se-bg://7654324", "amex faktura", dateFromNow(21), userId),
                createTransfer(credentialsId1, TransferType.EINVOICE, "se-bg://12345671", "telia", dateFromNow(35), userId));
    }

    private static List<Transfer> incomingEinvoiceWithChangedDate(String userId) {
        return Collections.singletonList(
                createTransfer(credentialsId1, TransferType.EINVOICE, "se-bg://12345671", "telia", dateFromNow(9), userId));
    }

    private static List<Transfer> incomingBankTransfer(String userId) {
        return Collections.singletonList(
                createTransfer(credentialsId1, TransferType.BANK_TRANSFER, "se://615212345671", "överföring", dateFromNow(9), userId));
    }

    public static class WhenOwningNoTransfersInDatabase {

        private TransferInMemoryRepository repository;

        @Before
        public void setUp() {

            List<Transfer> transfers = Collections.emptyList();
            repository = new TransferInMemoryRepository(transfers);
        }

        @Test
        public void whenNoIncoming_shouldChangeNothing() {
            TransferUseCases useCases = new TransferUseCases(repository);

            useCases.syncTransfersWithDatabase(userIdWithoutTransfers, tinkCredentialsId1, Collections
                    .<Transfer>emptyList());

            Set<UUID> result = Collections.emptySet();
            assertTrue(repository.containsNothingButTheseIds(result));
        }

        @Test
        public void whenTwoIncoming_shouldAddBoth() {
            TransferUseCases useCases = new TransferUseCases(repository);

            List<Transfer> incoming = twoIncomingEinvoices(userIdWithoutTransfers);

            useCases.syncTransfersWithDatabase(userIdWithoutTransfers, tinkCredentialsId1, incoming);

            Set<UUID> result = ImmutableSet.of(incoming.get(0).getId(), incoming.get(1).getId());
            assertTrue(repository.containsNothingButTheseIds(result));
        }

        @Test
        public void whenIncomingBankTransfer_shouldDoNothing() {
            TransferUseCases useCases = new TransferUseCases(repository);

            List<Transfer> incoming = incomingBankTransfer(userIdWithoutTransfers);
            useCases.syncTransfersWithDatabase(userIdWithoutTransfers, tinkCredentialsId1, incoming);

            assertTrue(repository.containsNothingButTheseIds(Collections.<UUID>emptySet()));
        }
    }


    public static class WhenOwningNoEInvoicesInDatabase {
        private TransferInMemoryRepository repository;

        @Before
        public void setUp() {

            List<Transfer> transfers = Arrays.asList(
                    createTransfer(id1, credentialsId1, TransferType.BANK_TRANSFER, "se://6123123456789", "överföring", dateFromNow(-4), userIdWithoutEInvoices),
                    createTransfer(id2, credentialsId1, TransferType.PAYMENT, "se-bg://7654324", "amex faktura", dateFromNow(-10), userIdWithoutEInvoices));

            repository = new TransferInMemoryRepository(transfers);
        }

        @Test
        public void whenNoIncoming_shouldChangeNothing() {
            TransferUseCases useCases = new TransferUseCases(repository);

            useCases.syncTransfersWithDatabase(userIdWithoutEInvoices, tinkCredentialsId1, Collections
                    .<Transfer>emptyList());
            assertTrue(repository.containsNothingButTheseIds(ImmutableSet.of(id1, id2)));
        }

        @Test
        public void whenTwoIncoming_shouldAddBoth() {
            TransferUseCases useCases = new TransferUseCases(repository);

            List<Transfer> incoming = twoIncomingEinvoices(userIdWithoutEInvoices);

            useCases.syncTransfersWithDatabase(userIdWithoutEInvoices, tinkCredentialsId1, incoming);

            Set<UUID> result = ImmutableSet.of(id1, id2, incoming.get(0).getId(), incoming.get(1).getId());
            assertTrue(repository.containsNothingButTheseIds(result));
        }
    }

    public static class WhenOwningAnEInvoiceInDatabase {
        private TransferInMemoryRepository repository;

        @Before
        public void setUp() {

            List<Transfer> transfers = Arrays.asList(
                    createTransfer(id3, credentialsId1, TransferType.EINVOICE, "se-bg://12345671", "telia", dateFromNow(5), userIdWithEInvoices),
                    createTransfer(id4, credentialsId1, TransferType.PAYMENT, "se-bg://12345671", "something", dateFromNow(-2), userIdWithEInvoices));

            repository = new TransferInMemoryRepository(transfers);
        }

        @Test
        public void whenNoIncoming_shouldRemoveEInvoices() {
            TransferUseCases useCases = new TransferUseCases(repository);

            useCases.syncTransfersWithDatabase(userIdWithEInvoices, tinkCredentialsId1, Collections
                    .<Transfer>emptyList());

            assertTrue(repository.containsNothingButTheseIds(ImmutableSet.of(id4)));
        }

        @Test
        public void whenIncomingBankTransfer_shouldRemoveEInvoices() {
            TransferUseCases useCases = new TransferUseCases(repository);

            List<Transfer> incoming = incomingBankTransfer(userIdWithEInvoices);
            useCases.syncTransfersWithDatabase(userIdWithEInvoices, tinkCredentialsId1, incoming);

            assertTrue(repository.containsNothingButTheseIds(ImmutableSet.of(id4)));
        }

        @Test
        public void whenTwoIncoming_shouldAddOnlyNewOnes() {
            TransferUseCases useCases = new TransferUseCases(repository);

            List<Transfer> incoming = twoIncomingEinvoices(userIdWithEInvoices);

            useCases.syncTransfersWithDatabase(userIdWithEInvoices, tinkCredentialsId1, incoming);

            Set<UUID> result = ImmutableSet.of(id3, id4, incoming.get(1).getId());
            assertTrue(repository.containsNothingButTheseIds(result));
        }

        @Test
        public void whenThreeIncoming_shouldAddOnlyNewOnes() {
            TransferUseCases useCases = new TransferUseCases(repository);

            List<Transfer> incoming = threeIncomingEinvoices(userIdWithEInvoices);

            useCases.syncTransfersWithDatabase(userIdWithEInvoices, tinkCredentialsId1, incoming);

            Set<UUID> result = ImmutableSet.of(id3, id4, incoming.get(1).getId(), incoming.get(2).getId());
            assertTrue(repository.containsNothingButTheseIds(result));
        }

        @Test
        public void whenOneIncomingWithChangedDate_shouldAddNewOneAndRemoveTheOldOne() {
            TransferUseCases useCases = new TransferUseCases(repository);

            List<Transfer> incoming = incomingEinvoiceWithChangedDate(userIdWithEInvoices);

            useCases.syncTransfersWithDatabase(userIdWithEInvoices, tinkCredentialsId1, incoming);

            Set<UUID> result = ImmutableSet.of(id4, incoming.get(0).getId());
            assertTrue(repository.containsNothingButTheseIds(result));
        }


        @Test
        public void whenPairOfDuplicatesIncoming_shouldRemoveOne_AndAddPairOfDuplicates() {
            TransferUseCases useCases = new TransferUseCases(repository);

            List<Transfer> incoming = pairOfDuplicateIncomingEinvoices(userIdWithEInvoices);

            useCases.syncTransfersWithDatabase(userIdWithEInvoices, tinkCredentialsId1, incoming);

            Set<UUID> result = ImmutableSet.of(id4, incoming.get(0).getId(), incoming.get(1).getId());
            assertTrue(repository.containsNothingButTheseIds(result));
        }

        @Test
        public void whenTwoPairsOfDuplicatesIncoming_shouldRemoveOne_AndAddBothPairsOfDuplicates() {
            TransferUseCases useCases = new TransferUseCases(repository);

            List<Transfer> incoming = twoPairsOfDuplicateIncomingEinvoices(userIdWithEInvoices);

            useCases.syncTransfersWithDatabase(userIdWithEInvoices, tinkCredentialsId1, incoming);

            Set<UUID> result = ImmutableSet.of(id4, incoming.get(0).getId(), incoming.get(1).getId(), incoming.get(2)
                    .getId(), incoming.get(3).getId());
            assertTrue(repository.containsNothingButTheseIds(result));
        }
    }

    public static class WhenOwningAnEInvoiceAndOtherUsersEinvoicesInDatabase {
        private TransferInMemoryRepository repository;

        @Before
        public void setUp() {

            List<Transfer> transfers = Arrays.asList(
                    createTransfer(id1, credentialsId1, TransferType.BANK_TRANSFER, "se://6123123456789", "överföring", dateFromNow(-4), userIdWithoutEInvoices),
                    createTransfer(id2, credentialsId1, TransferType.EINVOICE, "se-bg://7654324", "amex faktura", dateFromNow(-10), userIdWithoutEInvoices),
                    createTransfer(id3, credentialsId1, TransferType.EINVOICE, "se-bg://12345671", "telia", dateFromNow(5), userIdWithEInvoices),
                    createTransfer(id4, credentialsId1, TransferType.PAYMENT, "se-bg://12345671", "something", dateFromNow(-2), userIdWithEInvoices));

            repository = new TransferInMemoryRepository(transfers);
        }

        @Test
        public void whenNoIncoming_shouldRemoveEInvoices() {
            TransferUseCases useCases = new TransferUseCases(repository);

            useCases.syncTransfersWithDatabase(userIdWithEInvoices, tinkCredentialsId1, Collections
                    .<Transfer>emptyList());

            assertTrue(repository.containsNothingButTheseIds(ImmutableSet.of(id1, id2, id4)));
        }

        @Test
        public void whenTwoIncoming_shouldAddOnlyNewOnes() {
            TransferUseCases useCases = new TransferUseCases(repository);

            List<Transfer> incoming = twoIncomingEinvoices(userIdWithEInvoices);

            useCases.syncTransfersWithDatabase(userIdWithEInvoices, tinkCredentialsId1, incoming);

            Set<UUID> result = ImmutableSet.of(id1, id2, id3, id4, incoming.get(1).getId());
            assertTrue(repository.containsNothingButTheseIds(result));
        }

        @Test
        public void whenThreeIncoming_shouldAddOnlyNewOnes() {
            TransferUseCases useCases = new TransferUseCases(repository);

            List<Transfer> incoming = threeIncomingEinvoices(userIdWithEInvoices);

            useCases.syncTransfersWithDatabase(userIdWithEInvoices, tinkCredentialsId1, incoming);

            Set<UUID> result = ImmutableSet.of(id1, id2, id3, id4, incoming.get(1).getId(), incoming.get(2).getId());
            assertTrue(repository.containsNothingButTheseIds(result));
        }
    }

    public static class WhenOwningTwoEInvoicesFromDifferentCredentialsAndOtherUsersEinvoicesInDatabase {
        private TransferInMemoryRepository repository;

        @Before
        public void setUp() {

            List<Transfer> transfers = Arrays.asList(
                    createTransfer(id1, credentialsId1, TransferType.BANK_TRANSFER, "se://6123123456789", "överföring", dateFromNow(-4), userIdWithoutEInvoices),
                    createTransfer(id2, credentialsId1, TransferType.EINVOICE, "se-bg://7654324", "amex faktura", dateFromNow(-10), userIdWithoutEInvoices),
                    createTransfer(id3, credentialsId1, TransferType.EINVOICE, "se-bg://12345671", "telia", dateFromNow(5), userIdWithEInvoices),
                    createTransfer(id4, credentialsId1, TransferType.PAYMENT, "se-bg://12345671", "something", dateFromNow(-2), userIdWithEInvoices),
                    createTransfer(id5, credentialsId2, TransferType.EINVOICE, "se-bg://98765432", "bris", dateFromNow(5), userIdWithEInvoices));


            repository = new TransferInMemoryRepository(transfers);
        }

        @Test
        public void whenNoIncoming_shouldRemoveEInvoicesFromSameCredentials() {
            TransferUseCases useCases = new TransferUseCases(repository);

            useCases.syncTransfersWithDatabase(userIdWithEInvoices, tinkCredentialsId1, Collections
                    .<Transfer>emptyList());

            assertTrue(repository.containsNothingButTheseIds(ImmutableSet.of(id1, id2, id4, id5)));
        }

        @Test
        public void whenTwoIncoming_shouldAddOnlyNewOnes() {
            TransferUseCases useCases = new TransferUseCases(repository);

            List<Transfer> incoming = twoIncomingEinvoices(userIdWithEInvoices);

            useCases.syncTransfersWithDatabase(userIdWithEInvoices, tinkCredentialsId1, incoming);

            Set<UUID> result = ImmutableSet.of(id1, id2, id3, id4, id5, incoming.get(1).getId());
            assertTrue(repository.containsNothingButTheseIds(result));
        }

        @Test
        public void whenThreeIncoming_shouldAddOnlyNewOnes() {
            TransferUseCases useCases = new TransferUseCases(repository);

            List<Transfer> incoming = threeIncomingEinvoices(userIdWithEInvoices);

            useCases.syncTransfersWithDatabase(userIdWithEInvoices, tinkCredentialsId1, incoming);

            Set<UUID> result = ImmutableSet.of(id1, id2, id3, id4, id5, incoming.get(1).getId(), incoming.get(2).getId());
            assertTrue(repository.containsNothingButTheseIds(result));
        }
    }

    public static class whenOwningPairOfDuplicateEInvoicesInDatabase {
        private TransferInMemoryRepository repository;

        @Before
        public void setUp() {

            List<Transfer> transfers = Arrays.asList(
                    createTransfer(id2, credentialsId1, TransferType.EINVOICE, "se-bg://12345671", "telia",
                            dateFromNow(5), userIdWithEInvoices),
                    createTransfer(id3, credentialsId1, TransferType.EINVOICE, "se-bg://12345671", "telia",
                            dateFromNow(5), userIdWithEInvoices),
                    createTransfer(id4, credentialsId1, TransferType.PAYMENT, "se-bg://12345671", "something",
                            dateFromNow(-2), userIdWithEInvoices),
                    createTransfer(id5, credentialsId1, TransferType.EINVOICE, "se-bg://98765432", "bris",
                            dateFromNow(5), userIdWithEInvoices));


            repository = new TransferInMemoryRepository(transfers);
        }

        @Test
        public void whenOneIncoming_shouldRemovePairOfDuplicates_AndAddOne() {
            TransferUseCases useCases = new TransferUseCases(repository);

            List<Transfer> incoming = oneIncomingEinvoices(userIdWithEInvoices);

            useCases.syncTransfersWithDatabase(userIdWithEInvoices, tinkCredentialsId1, incoming);

            Set<UUID> result = ImmutableSet.of(id4, incoming.get(0).getId());
            assertTrue(repository.containsNothingButTheseIds(result));
        }

        @Test
        public void whenPairOfDuplicatesIncoming_shouldNotRemovePairOfDuplicate() {
            TransferUseCases useCases = new TransferUseCases(repository);

            List<Transfer> incoming = pairOfDuplicateIncomingEinvoices(userIdWithEInvoices);

            useCases.syncTransfersWithDatabase(userIdWithEInvoices, tinkCredentialsId1, incoming);

            Set<UUID> result = ImmutableSet.of(id2, id3, id4);
            assertTrue(repository.containsNothingButTheseIds(result));
        }

        @Test
        public void whenNoIncoming_shouldRemoveAllEinvoices() {
            TransferUseCases useCases = new TransferUseCases(repository);

            List<Transfer> incoming = Collections.emptyList();

            useCases.syncTransfersWithDatabase(userIdWithEInvoices, tinkCredentialsId1, incoming);

            Set<UUID> result = ImmutableSet.of(id4);
            assertTrue(repository.containsNothingButTheseIds(result));
        }
    }
}
