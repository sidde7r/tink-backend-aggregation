package se.tink.backend.aggregation.nxgen.core;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.joda.time.DateTime;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.core.Amount;
import se.tink.backend.core.SwedishGiroType;
import se.tink.backend.core.enums.TransferType;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.libraries.strings.StringUtils;
import se.tink.credentials.demo.DemoCredentials;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.date.DateUtils;

public class DemoData {
    private static final Splitter SPLITTER = Splitter.on('\t');
    private static final Charset DEFAULT_CHARSET = Charsets.UTF_8;
    public static final int MIN_DATA_SIZE = 3;
    public static final String FILENAME_ENDS_FOR_UNCHANGED_DATE = "ud";
    private static final int PENDING_FUTURE_TRANSACTION_CUTOFF = 31;

    public static List<Transaction> readTransactionsWithRandomization(DemoCredentials demoCredentials, File file,
            Account account, int transactionsToRandomize) throws IOException {

        List<Transaction> transactions = readTransactions(demoCredentials, file, account);

        Ordering<Transaction> orderingOnDate = Ordering.natural().onResultOf(Transaction::getDate);

        List<Transaction> lastTransactions = orderingOnDate.greatestOf(transactions, transactionsToRandomize);

        for (Transaction t : lastTransactions) {
            // Add some dummy randomization
            double prevAmount = t.getAmount().getValue();
            double randAmount = Math.round(prevAmount * Math.random());

            Transaction updatedTransaction = Transaction.builder()
                    .setAmount(Amount.inSEK(randAmount))
                    .setDate(t.getDate())
                    .setDescription(t.getDescription())
                    .setPending(t.isPending())
                    .build();

            transactions.remove(t);
            transactions.add(updatedTransaction);
        }

        return transactions;
    }

    public static List<Transaction> readTransactions(DemoCredentials credentials, File file,
            Account account) throws IOException {
        // Transaction file is not needed for loans
        if (account.getType() != null && Objects.equals(account.getType(), AccountTypes.LOAN)) {
            return Collections.emptyList();
        }

        List<Transaction> transactions = Lists.newArrayList();

        for (List<String> txFields : readTransactionsFrom(file)) {
            Date today = DateUtils.setInclusiveStartTime(new Date());
            Date transactionDate = parseDate(txFields.get(0), credentials, file, today);
            TransactionTypes transactionType = parseTransactionType(txFields);

            if (transactionDate.after(today)) {
                continue;
            }

            final Amount amount = Amount.inSEK(StringUtils.parseAmount(txFields.get(2)));
            final String description = txFields.get(1);
            final boolean pending = txFields.size() > 4 && Boolean.getBoolean(txFields.get(4));

            Transaction transaction;

            if (Objects.equals(transactionType, TransactionTypes.CREDIT_CARD)) {
                transaction = CreditCardTransaction.builder()
                        .setAmount(amount)
                        .setDate(transactionDate)
                        .setDescription(description)
                        .setPending(pending)
                        .build();
            } else {
                transaction = Transaction.builder()
                        .setAmount(amount)
                        .setDate(transactionDate)
                        .setDescription(description)
                        .setPending(pending)
                        .build();
            }

            transactions.add(transaction);
        }

        return transactions;
    }

    public static List<UpcomingTransaction> readUpcomingTransactions(DemoCredentials credentials, Account account,
            File file) throws IOException {
        // Upcoming transactions not needed for loans & credit-cards
        if (account.getType() != null && Objects.equals(account.getType(), AccountTypes.LOAN)
                || Objects.equals(account.getType(), AccountTypes.CREDIT_CARD)) {
            return Collections.emptyList();
        }

        List<UpcomingTransaction> upcomingTransactions = Lists.newArrayList();

        for (List<String> txFields : readTransactionsFrom(file)) {
            Date today = DateUtils.setInclusiveStartTime(new Date());
            Date transactionDate = parseDate(txFields.get(0), credentials, file, today);
            TransactionTypes transactionType = parseTransactionType(txFields);

            if (transactionDate.before(today) || !Objects.equals(transactionType, TransactionTypes.PAYMENT) ||
                    DateUtils.daysBetween(today, transactionDate) >= PENDING_FUTURE_TRANSACTION_CUTOFF) {
                continue;
            }

            String description = txFields.get(1);
            Transfer upcomingTransfer = null;

            if (description.equals("Hyra Stockholm")) {
                upcomingTransfer = createFakeTransfer("HSB Stockholm Ek", "1157002203", 1954, "3300308",
                        SwedishGiroType.BG, 7, TransferType.PAYMENT);
            }

            UpcomingTransaction upcomingTransaction = UpcomingTransaction.builder()
                    .setAmount(Amount.inSEK(StringUtils.parseAmount(txFields.get(2))))
                    .setDate(DateUtils.flattenTime(transactionDate))
                    .setDescription(description)
                    .setUpcomingTransfer(upcomingTransfer)
                    .build();

            upcomingTransactions.add(upcomingTransaction);
        }

        return upcomingTransactions;
    }

    public static Transfer createFakeTransfer(String name, String ocr, double amount, String giroNumber,
            SwedishGiroType giroType, int dueInDays, TransferType transferType) {
        Transfer transfer = new Transfer();
        transfer.setAmount(Amount.inSEK(amount));
        transfer.setDestinationMessage(ocr);
        transfer.setSourceMessage(name);
        transfer.setType(transferType);
        transfer.setDestination(AccountIdentifier.create(giroType.toAccountIdentifierType(), giroNumber));
        transfer.setDueDate(DateUtils.addDays(new Date(), dueInDays));

        return transfer;
    }

    private static TransactionTypes parseTransactionType(List<String> fields) {
        if (fields.size() <= 3) {
            return TransactionTypes.DEFAULT;
        }

        return Optional.ofNullable(Strings.emptyToNull(Strings.nullToEmpty(fields.get(3)).trim()))
                .map(TransactionTypes::valueOf)
                .orElse(TransactionTypes.DEFAULT);
    }

    private static Date parseDate(String dateField, DemoCredentials credentials, File file, Date today) {
        Calendar todayCal = Calendar.getInstance();
        todayCal.setTime(today);

        Date transDate;
        DateTime parsedDate = new DateTime(DateUtils.parseDate(dateField));

        if (credentials != null || file.getParentFile().getName().endsWith(FILENAME_ENDS_FOR_UNCHANGED_DATE)) {
            transDate = parsedDate.toDate();
        } else {
            int year = todayCal.get(Calendar.YEAR);
            transDate = parsedDate.withYear(year).toDate();
            if (transDate.getTime() > today.getTime()) {
                transDate = parsedDate.withYear(year - 1).toDate();
            }
        }

        return transDate;
    }

    private static List<List<String>> readTransactionsFrom(File file) throws IOException {
        return Files.readLines(file, DEFAULT_CHARSET).stream()
                .map(line -> Lists.newArrayList(SPLITTER.split(line)))
                .filter(fields -> fields.size() >= MIN_DATA_SIZE)
                .collect(Collectors.toList());
    }
}
